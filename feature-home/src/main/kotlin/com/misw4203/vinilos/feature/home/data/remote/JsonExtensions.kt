package com.misw4203.vinilos.feature.home.data.remote

import org.json.JSONArray
import org.json.JSONObject

internal fun JSONArray.safeOptJSONObject(index: Int): JSONObject? =
    runCatching { getJSONObject(index) }.getOrNull()

internal fun JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() }

internal fun JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null

internal fun JSONObject.optIntOrNull(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null
