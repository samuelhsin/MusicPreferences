/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sonyericsson.extras.liveware.extension.oss.music.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.sonyericsson.extras.liveware.extension.oss.music.MusicPreferenceActivity;
import com.sonyericsson.extras.liveware.extension.oss.music.R;

/**
 * The media intent receiver receives media intents and stores the latest
 * playback info.
 */
public class WalkmanMediaIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// This extra can be used to have many extension activities registered
		// in
		// the same apk

		String activityName = "com.sonyericsson.media.infinite.EXTRA_ACTIVITY_NAME";

		if (intent.getStringExtra(activityName) != null && intent.getStringExtra(activityName).equals(MusicPreferenceActivity.class.getName())) {

			Bundle extras = new Bundle();

			// Build a URI for the string resource for the description text
			String description = new Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(context.getPackageName()).appendPath(Integer.toString(R.string.description)).build()
					.toString();

			// And return it to the infinite framework as an extra
			extras.putString("com.sonyericsson.media.infinite.EXTRA_DESCRIPTION", description);
			setResultExtras(extras);

		}
	}
}
