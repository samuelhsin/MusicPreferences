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
import android.os.IBinder;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.sonyericsson.extras.liveware.extension.oss.music.R;
import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * The media button playback service is used to control playback on any media
 * player that supports media button intents.
 */
public class AutomaticMediaPlaybackService extends PlaybackService {

	public static final int PREFERENCE_ENTRY = R.string.preference_music_player_automatic;
	public static final int PREFERENCE_VALUE = R.string.preference_music_player_value_automatic;

	private static final String TAG = "AutomaticMedia: ";

	private final Context mContext;

	/**
	 * Create media button playback service.
	 * 
	 * @param context
	 *            The context.
	 */
	public AutomaticMediaPlaybackService(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		mContext = context;
		Dbg.d(TAG + "Instantiating Playback Service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean bindService() {
		// Inform listeners that new media info may be available.
		Intent mediaUpdateIntent = new Intent(MediaPlayerAdapter.ACTION_MEDIA_UPDATE);
		mContext.sendBroadcast(mediaUpdateIntent);

		// No service to bind to. Always successful.
		return true;
	}

	@Override
	public void unbindService() {
		// No service to unbind from.
	}

	@Override
	public String getArtistName() {
		return LatestMediaIntentInfo.getArtist(mContext);
	}

	@Override
	public String getTrackName() {
		return LatestMediaIntentInfo.getTrack(mContext);
	}

	@Override
	public boolean isPlaying() {
		return LatestMediaIntentInfo.getIsPlaying(mContext);
	}

	@Override
	public void pause() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
	}

	@Override
	public void play() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
	}

	@Override
	public void next() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_NEXT);
	}

	@Override
	public void prev() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
	}

	/**
	 * Send media button intent.
	 * 
	 * @param keyCode
	 *            The key code in the intent.
	 */
	private void sendMediaButtonIntent(int keyCode) {
		try {
			long eventtime = SystemClock.uptimeMillis();

			// Send down.
			Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
			downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
			mContext.sendOrderedBroadcast(downIntent, null);

			// Send up.
			Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0);
			upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
			mContext.sendOrderedBroadcast(upIntent, null);
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to send media button intent.", e);
		}
	}

	@Override
	public int getAlbumId() {
		return (int) LatestMediaIntentInfo.getAlbumId(mContext);
	}

	@Override
	public int getAudioId() {
		return (int) LatestMediaIntentInfo.getAudioId(mContext);
	}

}
