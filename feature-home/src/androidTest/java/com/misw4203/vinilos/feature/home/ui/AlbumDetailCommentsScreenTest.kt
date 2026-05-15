package com.misw4203.vinilos.feature.home.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.misw4203.vinilos.core.utils.model.RolePermissions
import com.misw4203.vinilos.core.utils.model.UserRole
import com.misw4203.vinilos.core.utils.model.UserSession
import com.misw4203.vinilos.core.utils.repository.SessionRepository
import com.misw4203.vinilos.core.utils.usecase.ObserveSessionUseCase
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.AlbumComment
import com.misw4203.vinilos.feature.home.domain.model.Collector
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.CollectorsRepository
import com.misw4203.vinilos.feature.home.domain.repository.CommentsRepository
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import com.misw4203.vinilos.feature.home.domain.usecase.AddTrackUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveAlbumDetailUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCollectorsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.ObserveCommentsUseCase
import com.misw4203.vinilos.feature.home.domain.usecase.PostCommentUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlbumDetailCommentsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun visitor_seesExistingComments_butNotComposer() {
        setContent(
            role = UserRole.VISITOR,
            comments = listOf(
                AlbumComment(id = 1L, description = "Loved this record", rating = 5),
            ),
        )

        // Comments section lives below the fold; assert via semantics existence.
        composeRule.onNodeWithText("Comments").assertExists()
        composeRule.onNodeWithText("Loved this record").assertExists()
        // Composer is hidden for Visitor.
        val postNodes = composeRule.onAllNodesWithTag(COMMENT_POST_TAG).fetchSemanticsNodes()
        assertTrue("Visitor must not see Post Comment button", postNodes.isEmpty())
        val inputNodes = composeRule.onAllNodesWithTag(COMMENT_INPUT_TAG).fetchSemanticsNodes()
        assertTrue("Visitor must not see comment input", inputNodes.isEmpty())
    }

    @Test
    fun collector_seesComposer_evenWithNoComments() {
        setContent(role = UserRole.COLLECTOR, comments = emptyList())

        composeRule.onNodeWithText("Comments").assertExists()
        composeRule.onNodeWithText("No comments yet. Be the first to share your take.").assertExists()
        composeRule.onNodeWithTag(COMMENT_INPUT_TAG).assertExists()
        composeRule.onNodeWithTag(COMMENT_POST_TAG).assertExists()
    }

    @Test
    fun postButton_isDisabled_whenDraftIsBlank() {
        setContent(role = UserRole.COLLECTOR)

        composeRule.onNodeWithTag(COMMENT_POST_TAG).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun postButton_enables_afterTypingDraft() {
        setContent(role = UserRole.COLLECTOR)

        composeRule.onNodeWithTag(COMMENT_INPUT_TAG).performScrollTo().performTextInput("Great record")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(COMMENT_POST_TAG).performScrollTo().assertIsEnabled()
    }

    @Test
    fun existingComment_isRenderedAsCommentRow_withTestTag() {
        val initial = AlbumComment(id = 7L, description = "Stunning record", rating = 5)
        setContent(role = UserRole.COLLECTOR, comments = listOf(initial))

        composeRule.onNodeWithTag(commentRowTag(7L)).assertExists()
        composeRule.onNodeWithText("Stunning record").assertExists()
    }

    // -- setup helpers --------------------------------------------------------

    private fun setContent(
        role: UserRole,
        comments: List<AlbumComment> = emptyList(),
        commentsRepository: FakeCommentsRepository = FakeCommentsRepository(initialComments = comments),
        collectors: List<Collector> = listOf(Collector(id = 1L, name = "Test", telephone = "1", email = "t@t.com")),
    ) {
        val album = HomeItem(
            id = "42",
            title = "Neon Midnight",
            artist = "The Synth Explorers",
            year = 2023,
            genre = "Electronic",
        )
        val viewModel = AlbumDetailViewModel(
            albumId = "42",
            observeAlbumDetailUseCase = ObserveAlbumDetailUseCase(FakeHomeRepository(album)),
            observeSessionUseCase = ObserveSessionUseCase(FakeSessionRepository(role)),
            observeCommentsUseCase = ObserveCommentsUseCase(commentsRepository),
            observeCollectorsUseCase = ObserveCollectorsUseCase(FakeCollectorsRepository(collectors)),
            addTrackUseCase = AddTrackUseCase(FakeHomeRepository(album)),
            postCommentUseCase = PostCommentUseCase(commentsRepository),
        )
        composeRule.setContent {
            MaterialTheme {
                AlbumDetailScreen(viewModel = viewModel, onBack = {})
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertTrue(message: String, condition: Boolean) {
        if (!condition) throw AssertionError(message)
    }

    private class FakeHomeRepository(private val album: HomeItem) : HomeRepository {
        override fun observeItems(): Flow<List<HomeItem>> = flowOf(listOf(album))
        override fun observeItem(id: String): Flow<HomeItem?> = flowOf(album)
        override suspend fun createAlbum(album: AlbumDto): Boolean = true
        override suspend fun uploadCover(contentResolver: android.content.ContentResolver, uriString: String): String? = null
        override suspend fun addTrack(albumId: String, name: String, duration: String): Boolean = true
    }

    private class FakeSessionRepository(private val role: UserRole) : SessionRepository {
        private val permissions = when (role) {
            UserRole.COLLECTOR -> RolePermissions(canCreate = true, canEdit = true, canDelete = true)
            UserRole.VISITOR -> RolePermissions()
        }
        private val flow = MutableStateFlow<UserSession?>(UserSession(role = role, permissions = permissions))
        override fun observeSession(): Flow<UserSession?> = flow
        override suspend fun saveRole(role: UserRole) { }
        override suspend fun clearSession() { flow.value = null }
    }

    private class FakeCollectorsRepository(private val collectors: List<Collector>) : CollectorsRepository {
        override fun observeCollectors(): Flow<List<Collector>> = MutableStateFlow(collectors)
    }

    private class FakeCommentsRepository(
        initialComments: List<AlbumComment> = emptyList(),
        private val postResult: AlbumComment? = AlbumComment(id = 99L, description = "x", rating = 4),
    ) : CommentsRepository {
        private val flow = MutableStateFlow(initialComments)
        override fun observeComments(albumId: String): Flow<List<AlbumComment>> = flow
        override suspend fun postComment(
            albumId: String,
            description: String,
            rating: Int,
            collectorId: Long,
        ): AlbumComment? {
            val created = postResult?.copy(description = description, rating = rating)
            if (created != null) flow.value = flow.value + created
            return created
        }
    }
}
