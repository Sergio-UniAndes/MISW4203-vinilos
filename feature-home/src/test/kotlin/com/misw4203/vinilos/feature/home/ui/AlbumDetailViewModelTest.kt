package com.misw4203.vinilos.feature.home.ui

import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.model.Collector
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository
import com.misw4203.vinilos.feature.home.domain.repository.CollectorsRepository
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.AddTrackUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCollectorsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCommentsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.PostCommentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule: TestWatcher = AlbumDetailMainDispatcherRule()

    // -- existing tests -------------------------------------------------------

    @Test
    fun initialState_isLoadingWithNoAlbum() {
        val viewModel = buildViewModel()

        val initial = viewModel.uiState.value
        assertTrue(initial.isLoading)
        assertNull(initial.album)
    }

    @Test
    fun whenAlbumEmits_stateExposesAlbumAndStopsLoading() = runTest {
        val albumFlow = MutableStateFlow<HomeItem?>(null)
        val viewModel = buildViewModel(albumFlow = albumFlow)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        albumFlow.value = sampleAlbum()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("42", state.album?.id)
        assertEquals("Sample", state.album?.title)

        collectJob.cancel()
    }

    @Test
    fun whenAlbumIsMissing_stateExposesNullAlbum() = runTest {
        val viewModel = buildViewModel()
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.album)

        collectJob.cancel()
    }

    // -- permissions ----------------------------------------------------------

    @Test
    fun canCreate_isFalse_forVisitorSession() = runTest {
        val viewModel = buildViewModel(role = UserRole.VISITOR)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canCreate)

        collectJob.cancel()
    }

    @Test
    fun canCreate_isTrue_forCollectorSession() = runTest {
        val viewModel = buildViewModel(role = UserRole.COLLECTOR)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.canCreate)

        collectJob.cancel()
    }

    // -- add track dialog state -----------------------------------------------

    @Test
    fun onAddTrackClick_opensDialog() = runTest {
        val viewModel = buildViewModel()
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAddTrackDialog)
        collectJob.cancel()
    }

    @Test
    fun onDismissAddTrack_closesDialog() = runTest {
        val viewModel = buildViewModel()
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        advanceUntilIdle()
        viewModel.onDismissAddTrack()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddTrackDialog)
        collectJob.cancel()
    }

    @Test
    fun onTrackNameChange_updatesState() = runTest {
        val viewModel = buildViewModel()
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        viewModel.onTrackNameChange("Decisiones")
        advanceUntilIdle()

        assertEquals("Decisiones", viewModel.uiState.value.trackName)
        collectJob.cancel()
    }

    @Test
    fun onConfirmAddTrack_withBlankName_setsError() = runTest {
        val viewModel = buildViewModel()
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        viewModel.onConfirmAddTrack()
        advanceUntilIdle()

        assertEquals("Track name is required", viewModel.uiState.value.addTrackError)
        assertTrue(viewModel.uiState.value.showAddTrackDialog)
        collectJob.cancel()
    }

    @Test
    fun onConfirmAddTrack_onSuccess_closesDialogAndEmitsEffect() = runTest {
        val viewModel = buildViewModel(addTrackResult = true)
        val effects = mutableListOf<AlbumDetailUiEffect>()
        val effectJob = backgroundScope.launch { viewModel.effects.collect { effects += it } }
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        viewModel.onTrackNameChange("Decisiones")
        viewModel.onTrackDurationChange("4:30")
        viewModel.onConfirmAddTrack()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddTrackDialog)
        assertEquals(1, effects.size)
        assertEquals(AlbumDetailUiEffect.TrackAdded, effects[0])

        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun onConfirmAddTrack_onFailure_showsError() = runTest {
        val viewModel = buildViewModel(addTrackResult = false)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAddTrackClick()
        viewModel.onTrackNameChange("Decisiones")
        viewModel.onConfirmAddTrack()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showAddTrackDialog)
        assertFalse(viewModel.uiState.value.isAddingTrack)
        assertTrue(viewModel.uiState.value.addTrackError?.isNotBlank() == true)

        collectJob.cancel()
    }

    // -- comments -------------------------------------------------------------

    @Test
    fun commentsFromRepository_areExposedInState() = runTest {
        val initialComments = listOf(AlbumComment(id = 1L, description = "Loved it", rating = 5))
        val viewModel = buildViewModel(comments = initialComments)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.comments.size)
        assertEquals("Loved it", state.comments[0].description)

        collectJob.cancel()
    }

    @Test
    fun onCommentDraftChange_updatesDraft_andClearsError() = runTest {
        val viewModel = buildViewModel(role = UserRole.COLLECTOR)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onCommentDraftChange("Detailed take")
        advanceUntilIdle()

        assertEquals("Detailed take", viewModel.uiState.value.commentDraft)
        assertNull(viewModel.uiState.value.commentError)
        collectJob.cancel()
    }

    @Test
    fun onCommentRatingChange_clampsToZeroFiveRange() = runTest {
        val viewModel = buildViewModel(role = UserRole.COLLECTOR)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onCommentRatingChange(-3)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.commentRating)

        viewModel.onCommentRatingChange(9)
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.commentRating)

        collectJob.cancel()
    }

    @Test
    fun onPostComment_withEmptyDraft_setsError_andDoesNotCallRepository() = runTest {
        val repository = FakeCommentsRepository()
        val viewModel = buildViewModel(role = UserRole.COLLECTOR, commentsRepository = repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onPostComment()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.commentError?.isNotBlank() == true)
        assertFalse(repository.postCalled)
        collectJob.cancel()
    }

    @Test
    fun onPostComment_withNoCollectors_setsError() = runTest {
        val viewModel = buildViewModel(role = UserRole.COLLECTOR, collectors = emptyList())
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onCommentDraftChange("Great record")
        viewModel.onPostComment()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.commentError?.isNotBlank() == true)
        assertFalse(viewModel.uiState.value.isPostingComment)
        collectJob.cancel()
    }

    @Test
    fun onPostComment_onSuccess_clearsDraft_emitsEffect() = runTest {
        val repository = FakeCommentsRepository(
            postResult = AlbumComment(id = 99L, description = "Great record", rating = 4),
        )
        val viewModel = buildViewModel(role = UserRole.COLLECTOR, commentsRepository = repository)
        val effects = mutableListOf<AlbumDetailUiEffect>()
        val effectJob = backgroundScope.launch { viewModel.effects.collect { effects += it } }
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onCommentDraftChange("Great record")
        viewModel.onCommentRatingChange(4)
        viewModel.onPostComment()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.commentDraft)
        assertEquals(AlbumDetailUiState.DEFAULT_COMMENT_RATING, state.commentRating)
        assertFalse(state.isPostingComment)
        assertNull(state.commentError)
        assertTrue(repository.postCalled)
        assertEquals("Great record", repository.lastDescription)
        assertEquals(4, repository.lastRating)
        assertEquals(1L, repository.lastCollectorId)
        assertEquals(listOf<AlbumDetailUiEffect>(AlbumDetailUiEffect.CommentPosted), effects)

        collectJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun onPostComment_onNetworkFailure_setsError_keepsDraft() = runTest {
        val repository = FakeCommentsRepository(postResult = null)
        val viewModel = buildViewModel(role = UserRole.COLLECTOR, commentsRepository = repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onCommentDraftChange("Great record")
        viewModel.onPostComment()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Great record", state.commentDraft)
        assertFalse(state.isPostingComment)
        assertTrue(state.commentError?.isNotBlank() == true)

        collectJob.cancel()
    }

    // -- helpers --------------------------------------------------------------

    private fun buildViewModel(
        albumFlow: MutableStateFlow<HomeItem?> = MutableStateFlow(null),
        role: UserRole = UserRole.VISITOR,
        addTrackResult: Boolean = true,
        comments: List<AlbumComment> = emptyList(),
        collectors: List<Collector> = listOf(Collector(id = 1L, name = "Test", telephone = "1", email = "t@t.com")),
        commentsRepository: FakeCommentsRepository = FakeCommentsRepository(initialComments = comments),
    ): AlbumDetailViewModel {
        val repository = FakeAlbumRepository(albumFlow, addTrackResult = addTrackResult)
        val sessionRepository = AlbumDetailFakeSessionRepository(role)
        val collectorsRepository = FakeCollectorsRepository(collectors)
        return AlbumDetailViewModel(
            albumId = "42",
            observeAlbumDetailUseCase = ObserveAlbumDetailUseCase(repository),
            observeSessionUseCase = ObserveSessionUseCase(sessionRepository),
            observeCommentsUseCase = ObserveCommentsUseCase(commentsRepository),
            observeCollectorsUseCase = ObserveCollectorsUseCase(collectorsRepository),
            addTrackUseCase = AddTrackUseCase(repository),
            postCommentUseCase = PostCommentUseCase(commentsRepository),
        )
    }

    private fun sampleAlbum(): HomeItem = HomeItem(
        id = "42",
        title = "Sample",
        artist = "Artist",
        year = 2020,
        genre = "Rock",
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
private class AlbumDetailMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeAlbumRepository(
    private val flow: MutableStateFlow<HomeItem?>,
    private val addTrackResult: Boolean = true,
) : HomeRepository {
    val requestedIds = mutableListOf<String>()

    override fun observeItems(): Flow<List<HomeItem>> = MutableStateFlow(emptyList())

    override fun observeItem(id: String): Flow<HomeItem?> {
        requestedIds += id
        return flow
    }

    override suspend fun createAlbum(album: com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto): Boolean = true

    override suspend fun uploadCover(contentResolver: android.content.ContentResolver, uriString: String): String? = null

    override suspend fun addTrack(albumId: String, name: String, duration: String): Boolean = addTrackResult
}

private class AlbumDetailFakeSessionRepository(private val role: UserRole) : SessionRepository {
    private val permissions = when (role) {
        UserRole.COLLECTOR -> RolePermissions(canCreate = true, canEdit = true, canDelete = true)
        UserRole.VISITOR -> RolePermissions()
    }
    private val session = UserSession(role = role, permissions = permissions)
    private val flow = MutableStateFlow<UserSession?>(session)

    override fun observeSession(): Flow<UserSession?> = flow
    override suspend fun saveRole(role: UserRole) { }
    override suspend fun clearSession() { flow.value = null }
}

private class FakeCollectorsRepository(
    private val collectors: List<Collector>,
) : CollectorsRepository {
    override fun observeCollectors(): Flow<List<Collector>> = MutableStateFlow(collectors)
}

internal class FakeCommentsRepository(
    initialComments: List<AlbumComment> = emptyList(),
    private val postResult: AlbumComment? = AlbumComment(id = 1L, description = "x", rating = 4),
) : CommentsRepository {
    private val commentsFlow = MutableStateFlow(initialComments)

    var postCalled: Boolean = false
    var lastDescription: String? = null
    var lastRating: Int? = null
    var lastCollectorId: Long? = null

    override fun observeComments(albumId: String): Flow<List<AlbumComment>> = commentsFlow

    override suspend fun postComment(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long,
    ): AlbumComment? {
        postCalled = true
        lastDescription = description
        lastRating = rating
        lastCollectorId = collectorId
        if (postResult != null) {
            commentsFlow.value = commentsFlow.value + postResult
        }
        return postResult
    }
}
