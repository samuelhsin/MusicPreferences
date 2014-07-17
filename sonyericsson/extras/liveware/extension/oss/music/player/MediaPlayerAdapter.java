/*
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2012, Sony Mobile Communications AB.

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

import java.util.ArrayList;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.music.MusicUtils;
import com.sonyericsson.extras.liveware.extension.oss.music.R;
import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * The media player adapter provides a generic way to control the media player
 * regardless of which media player that is actually used.
 */
public class MediaPlayerAdapter {

	public static final String ACTION_MEDIA_UPDATE = "com.sonyericsson.extras.liveware.extension.oss.music.player.update";

	private final Context mContext;

	private PlaybackService mPlaybackService = null;

	private MediaReceiver mReceiver;

	private final ArrayList<PlaybackListener> mListeners = new ArrayList<PlaybackListener>();

	private PreferenceListener mPreferenceListener;

	/**
	 * Create media player adapter.
	 * 
	 * @param context
	 *            The context.
	 */
	public MediaPlayerAdapter(final Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		mContext = context;

		// Start listening for media intents
		mReceiver = new MediaReceiver();
		mContext.registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_UPDATE));

		// Start listening for preference changes to check if user has selected
		// another playback service.
		mPreferenceListener = new PreferenceListener();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		preferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);
	}

	/**
	 * Destroy the media player adapter.
	 */
	public void onDestroy() {

		// Stop listening for media intents.
		if (mReceiver != null) {
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		// Unbind with service.
		if (mPlaybackService != null) {
			mPlaybackService.unbindService();
		}

		// Unregister preference listener
		if (mPreferenceListener != null) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			preferences.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
			mPreferenceListener = null;
		}

	}

	/**
	 * Start and bind to media playback service.
	 * 
	 * @return True if successfully bound to service.
	 */
	public boolean startAndBindToMediaService() {
		Dbg.d("startAndBindToMediaService");
		Dbg.d("Version " + Build.VERSION.SDK_INT + " " + Build.MANUFACTURER);

		if (mPlaybackService != null) {
			mPlaybackService.unbindService();
		}
		mPlaybackService = null;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String preferenceKey = mContext.getString(R.string.preference_music_player_key);
		String preferenceValue = preferences.getString(preferenceKey, "-1");

		boolean isBound = false;

		if (preferenceValue.equals(mContext.getString(AutomaticMediaPlaybackService.PREFERENCE_VALUE))) {
			// User has selected media button emulation
			mPlaybackService = new AutomaticMediaPlaybackService(mContext);
			isBound = mPlaybackService.bindService();
		} else if (preferenceValue.equals(mContext.getString(WalkmanMediaPlaybackService.PREFERENCE_VALUE))) {
			// User has selected Android generic
			mPlaybackService = new WalkmanMediaPlaybackService(mContext);
			isBound = mPlaybackService.bindService();
		} else if (preferenceValue.equals(mContext.getString(KKBoxMediaPlaybackService.PREFERENCE_VALUE))) {
			// User has selected Android generic
			mPlaybackService = new KKBoxMediaPlaybackService(mContext);
			isBound = mPlaybackService.bindService();
		}else if (preferenceValue.equals(mContext.getString(GenericPlaybackService.PREFERENCE_VALUE))) {
			// User has selected Android generic
			mPlaybackService = new GenericPlaybackService(mContext);
			isBound = mPlaybackService.bindService();
		} else {
			// User has not yet made a choice.
			// Try to make an intelligent choice.

			int valueResourceId = -1;

			// Try Android generic
			if (!isBound) {
				if (mPlaybackService != null) {
					mPlaybackService.unbindService();
				}
				mPlaybackService = new GenericPlaybackService(mContext);
				isBound = mPlaybackService.bindService();
				if (isBound) {
					valueResourceId = GenericPlaybackService.PREFERENCE_VALUE;
				}
			}

			// If not bound to any of the built in players then fall back to
			// media button.
			if (!isBound) {
				mPlaybackService = new AutomaticMediaPlaybackService(mContext);
				isBound = mPlaybackService.bindService();
				if (isBound) {
					valueResourceId = AutomaticMediaPlaybackService.PREFERENCE_VALUE;
				}
			}

			// Set the preference value.
			if (isBound) {
				preferences.edit().putString(mContext.getString(R.string.preference_music_player_key), mContext.getString(valueResourceId)).commit();
			}
		}

		// If not bound to any player then fall back to media button.
		// This should only happen in exceptional cases.
		if (!isBound) {
			Dbg.e("Playback service bind error: Fallback to media button.");
			mPlaybackService = new AutomaticMediaPlaybackService(mContext);
			isBound = mPlaybackService.bindService();
		}

		return isBound;
	}

	/**
	 * Start playback.
	 */
	public void play() {
		if (mPlaybackService != null) {
			mPlaybackService.play();
		}
	}

	/**
	 * Is the media player currently playing.
	 * 
	 * @return True if media player is playing.
	 */
	public boolean isPlaying() {
		if (mPlaybackService == null) {
			return false;
		}
		return mPlaybackService.isPlaying();
	}

	/**
	 * Pause playback.
	 */
	public void pause() {
		if (mPlaybackService != null) {
			mPlaybackService.pause();
		}
	}

	/**
	 * Change to previous track.
	 */
	public void prev() {
		if (mPlaybackService != null) {
			mPlaybackService.prev();
		}
	}

	/**
	 * Change to next track.
	 */
	public void next() {
		if (mPlaybackService != null) {
			mPlaybackService.next();
		}
	}

	/**
	 * Get the title of the current track.
	 * 
	 * @return Title of the current track.
	 */
	public String getTitle() {
		String track = null;
		if (mPlaybackService != null) {
			track = mPlaybackService.getTrackName();
		}
		if (track == null) {
			track = mContext.getString(R.string.unknown_track);
		}

		return track;
	}

	/**
	 * Get the artist of the current track.
	 * 
	 * @return Artist of the current track.
	 */
	public String getArtist() {
		String artist = null;
		if (mPlaybackService != null) {
			artist = mPlaybackService.getArtistName();
		}
		if (artist == null) {
			artist = mContext.getString(R.string.unknown_artist);
		}

		return artist;
	}

	/**
	 * Get the album art for the current track.
	 * 
	 * @return The album art for the current track.
	 */
	public Bitmap getAlbumArt() {
		Bitmap albumArt = null;
		long albumId = PlaybackService.INVALID_ALBUM_ID;
		long audioId = PlaybackService.INVALID_AUDIO_ID;
		if (mPlaybackService != null) {
			albumId = mPlaybackService.getAlbumId();
			audioId = mPlaybackService.getAudioId();
		}

		if (albumId != PlaybackService.INVALID_ALBUM_ID || audioId != PlaybackService.INVALID_AUDIO_ID) {
			albumArt = MusicUtils.getArtwork(mContext, audioId, albumId);
		}

		if (albumArt == null) {
			albumArt = MusicUtils.getDefaultArtwork(mContext);
		}

		return albumArt;
	}

	/**
	 * Register playback listener.
	 * 
	 * @param listener
	 *            The playback listener.
	 */
	public void registerListener(PlaybackListener listener) {
		mListeners.add(listener);
	}

	/**
	 * Unregister playback listener.
	 * 
	 * @param listener
	 *            The playback listener.
	 */
	public void unregisterListener(PlaybackListener listener) {
		mListeners.remove(listener);
	}

	/**
	 * The media receiver receives media intents.
	 */
	private class MediaReceiver extends BroadcastReceiver {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, final Intent intent) {
			Dbg.d("MediaIntent: " + intent.getAction());

			// Make sure to refresh info.
			Iterator<PlaybackListener> iterator = mListeners.iterator();
			while (iterator.hasNext()) {
				iterator.next().onUpdate();
			}

		}
	}

	/**
	 * Listens to preference changes and updates the running threads with the
	 * new data.
	 */
	private class PreferenceListener implements OnSharedPreferenceChangeListener {
		private String mCurrentPlayer = null;

		/**
		 * Create preference listener.
		 */
		public PreferenceListener() {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			mCurrentPlayer = preferences.getString(mContext.getString(R.string.preference_music_player_key), null);
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (key.equals(mContext.getString(R.string.preference_music_player_key))) {
				String musicPlayer = prefs.getString(key, mContext.getString(R.string.preference_music_player_value_automatic));
				Dbg.d("Music player changed " + musicPlayer);

				if (!TextUtils.equals(mCurrentPlayer, musicPlayer)) {
					mCurrentPlayer = musicPlayer;
					// Bind to new media player service.
					startAndBindToMediaService();
				}
			}
		}
	}

}
