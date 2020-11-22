/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.AllArgsConstructor;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@AllArgsConstructor
class SessionClient
{
	private final OkHttpClient okHttpClient;

	Observable<UUID> openSession()
	{
		final HttpUrl url = RuneLiteAPI.getSessionBase().newBuilder()
			.addPathSegment("new")
			.build();

		return Observable.fromCallable(() ->
		{
			Request request = new Request.Builder()
				.url(url)
				.build();

			try (Response response = okHttpClient.newCall(request).execute())
			{
				ResponseBody body = response.body();

				InputStream in = body.byteStream();
				try
				{
					return RuneLiteAPI.GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), UUID.class);
				}
				catch (IllegalArgumentException ex)
				{
					ex.printStackTrace();
					return null;
				}
			}
		});
	}

	Completable pingSession(UUID uuid)
	{
		final HttpUrl url = RuneLiteAPI.getSessionBase().newBuilder()
			.addPathSegment("ping")
			.addQueryParameter("uuid", uuid.toString())
			.build();

		return Completable.fromAction(() ->
		{
			Request request = new Request.Builder()
				.url(url)
				.build();

			try (Response response = okHttpClient.newCall(request).execute())
			{
				if (!response.isSuccessful())
				{
					throw new IOException("Unsuccessful ping");
				}
			}
		});
	}

	Completable delete(UUID uuid)
	{
		final HttpUrl url = RuneLiteAPI.getSessionBase().newBuilder()
			.addQueryParameter("session", uuid.toString())
			.build();

		return Completable.fromAction(() ->
		{
			Request request = new Request.Builder()
				.delete()
				.url(url)
				.build();

			okHttpClient.newCall(request).execute().close();
		});
	}
}
