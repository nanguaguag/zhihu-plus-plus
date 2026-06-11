/*
 * Zhihu++ - Free & Ad-Free Zhihu client for Android.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.github.zly2006.zhihu.data.AccountData
import com.github.zly2006.zhihu.shared.data.ZhihuJson.json
import com.github.zly2006.zhihu.shared.platform.androidSettingsStore
import com.github.zly2006.zhihu.shared.util.ZHIHU_WEB_ZSE93
import com.github.zly2006.zhihu.shared.util.signZhihuFetchRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.serializer

fun HttpRequestBuilder.signFetchRequest() {
    val body = if (contentType() == ContentType.Application.Json) {
        body as? String
            ?: bodyType?.kotlinType?.let { type ->
                json.encodeToString(serializer(type), body)
            }
    } else {
        null
    }
    signZhihuFetchRequest(
        zse93 = ZHIHU_WEB_ZSE93,
        dc0 = AccountData.data.cookies["d_c0"] ?: "",
        body = body,
    )
}

/**
 * 洛天依主题浏览器打开
 */
fun luoTianYiUrlLauncher(context: Context, uri: Uri) {
    if (uri.host == "link.zhihu.com") {
        Url(uri.toString()).parameters["target"]?.let {
            luoTianYiUrlLauncher(context, it.toUri())
            return
        }
    }
    val color = androidSettingsStore(context).getInt("luotianyi_color", 0xff_66CCFF.toInt())
    val intent = CustomTabsIntent
        .Builder()
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams
                .Builder()
                .setToolbarColor(color)
                .build(),
        ).build()
    intent.launchUrl(context, uri)
}

val Context.clipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
