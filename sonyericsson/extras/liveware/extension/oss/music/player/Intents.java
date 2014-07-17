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
package com.sonyericsson.extras.liveware.extension.oss.music.player;

import android.content.Context;
import android.content.Intent;

import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * The intents class contains play back intents for various different media
 * players.
 */
public class Intents {

	/**
	 * Intents for generic Android media player.
	 */
	public interface Generic {
		public static final String ACTION_PLAY_STATE_CHANGED = "com.android.music.playstatechanged";

		public static final String ACTION_PLAYBACK_COMPLETE = "com.android.music.playbackcomplete";

		public static final String ACTION_META_CHANGED = "com.android.music.metachanged";

		public static final String EXTRA_IS_PLAYING = "isplaying";

		public static final String EXTRA_ARTIST = "artist";

		public static final String EXTRA_TITLE = "track";

		public static final String EXTRA_ID = "id";

	}

	/**
	 * Parse the playback info from the intent.
	 * 
	 * @param context
	 *            The context.
	 * @param intent
	 *            The intent.
	 * 
	 * @return The playback info from the intent.
	 */
	public static PlaybackInfo parse(final Context context, final Intent intent) {
		PlaybackInfo info = null;

		String action = intent.getAction();

		// Note! Some intents are just used to trigger an update of
		// the user interface by reading new values from the manager.
		// Those intents does not contain any information that we need to
		// save, so they are not handled here.

		if (Intents.Generic.ACTION_PLAY_STATE_CHANGED.equals(action)) {
			info = new PlaybackInfo();
			if (intent.hasExtra(Intents.Generic.EXTRA_IS_PLAYING)) {
				info.mIsPlaying = intent.getBooleanExtra(Intents.Generic.EXTRA_IS_PLAYING, false);
			} else {
				// Toggle playback status
				info.mIsPlaying = !LatestMediaIntentInfo.getIsPlaying(context);
			}
			info.mArtist = intent.getStringExtra(Intents.Generic.EXTRA_ARTIST);
			info.mTrack = intent.getStringExtra(Intents.Generic.EXTRA_TITLE);
			info.mAudioId = intent.getLongExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			if (info.mAudioId == PlaybackService.INVALID_AUDIO_ID) {
				info.mAudioId = intent.getIntExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			}
			Dbg.d("audioId: " + info.mAudioId);
		} else if (Intents.Generic.ACTION_META_CHANGED.equals(action)) {
			info = new PlaybackInfo();
			// Play status unchanged.
			info.mArtist = intent.getStringExtra(Intents.Generic.EXTRA_ARTIST);
			info.mTrack = intent.getStringExtra(Intents.Generic.EXTRA_TITLE);
			info.mAudioId = intent.getLongExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			if (info.mAudioId == PlaybackService.INVALID_AUDIO_ID) {
				info.mAudioId = intent.getIntExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			}
		} else if (Intents.Generic.ACTION_PLAYBACK_COMPLETE.equals(action)) {
			info = new PlaybackInfo();
			info.mIsPlaying = false;
			info.mArtist = intent.getStringExtra(Intents.Generic.EXTRA_ARTIST);
			info.mTrack = intent.getStringExtra(Intents.Generic.EXTRA_TITLE);
			info.mAudioId = intent.getLongExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			if (info.mAudioId == PlaybackService.INVALID_AUDIO_ID) {
				info.mAudioId = intent.getIntExtra(Intents.Generic.EXTRA_ID, PlaybackService.INVALID_AUDIO_ID);
			}
		}
		return info;
	}

}
