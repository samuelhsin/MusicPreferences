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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.music.IMediaPlaybackService;
import com.sonyericsson.extras.liveware.extension.oss.music.R;
import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * The generic playback service is used to control the playback on the generic
 * media player included in vanilla Android pre Honeycomb. Honeycomb includes a
 * different media player and the only way we currently can control this media
 * player is through the MediaButtonPlaybackService.
 */
public class GenericPlaybackService extends PlaybackService {

	public static final int PREFERENCE_ENTRY = R.string.preference_music_player_generic;
	public static final int PREFERENCE_VALUE = R.string.preference_music_player_value_generic;

	private static final String PACKAGE_NAME = "com.android.music";
	private static final String CLASS_NAME = "com.android.music.MediaPlaybackService";

	private static final String TAG = "GenericMedia: ";

	private IMediaPlaybackService mManager = null;

	private final Context mContext;

	/**
	 * Service connection to the playback service.
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder iBinder) {
			mManager = com.android.music.IMediaPlaybackService.Stub.asInterface(iBinder);

			// Inform listeners that new media info may be available.
			Intent mediaUpdateIntent = new Intent(MediaPlayerAdapter.ACTION_MEDIA_UPDATE);
			mContext.sendBroadcast(mediaUpdateIntent);
		}

		public void onServiceDisconnected(ComponentName name) {
			mManager = null;
		}
	};

	/**
	 * Create generic playback service.
	 * 
	 * @param context
	 *            The context.
	 */
	public GenericPlaybackService(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		mContext = context;
		Dbg.d(TAG + "Instantiating Playback Service");
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public boolean bindService() {
		boolean result = false;
		Intent i = new Intent("com.android.music.IMediaPlaybackService");
		try {
			//i.setClassName(PACKAGE_NAME, CLASS_NAME);

			result = mContext.bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			Dbg.d(TAG + "Bind exception");
		}

		if (!result) {
			Dbg.d(TAG + "Failed to bind.");
		}

		return result;
	}

	@Override
	public void unbindService() {
		mContext.unbindService(mServiceConnection);
	}

	@Override
	public String getArtistName() {
		String result = null;
		try {
			if (mManager != null) {
				result = mManager.getArtistName();
			}
		} catch (RemoteException re) {
			Dbg.e(TAG + "Failed to get artist name", re);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to get artist name", e);
		}

		return result;
	}

	@Override
	public String getTrackName() {
		String result = null;
		try {
			if (mManager != null) {
				result = mManager.getTrackName();
			}
		} catch (RemoteException re) {
			Dbg.e(TAG + "Failed to get track name", re);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to get track name", e);
		}

		return result;
	}

	@Override
	public boolean isPlaying() {
		boolean result = false;
		try {
			if (mManager != null) {
				result = mManager.isPlaying();
			}
		} catch (RemoteException re) {
			Dbg.e(TAG + "Failed to check isPlaying", re);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to check isPlaying", e);
		}

		return result;
	}

	@Override
	public void next() {
		try {
			if (mManager != null) {
				mManager.next();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to play next track", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to play next track", e);
		}
	}

	@Override
	public void pause() {
		try {
			if (mManager != null) {
				mManager.pause();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to pause track", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to pause track", e);
		}
	}

	@Override
	public void play() {
		try {
			if (mManager != null) {
				mManager.play();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to play track", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to play track", e);
		}
	}

	@Override
	public void prev() {
		try {
			if (mManager != null) {
				mManager.prev();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to go to prev track", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to go to prev track", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sonyericsson.extras.liveware.extension.oss.music.player.
	 * PlaybackServiceInterface#getAlbumId()
	 */
	@Override
	public int getAlbumId() {
		int result = INVALID_ALBUM_ID;
		try {
			if (mManager != null) {
				result = mManager.getAlbumId();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to get album id", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to get album id", e);
		}

		return result;
	}

	@Override
	public int getAudioId() {
		int result = INVALID_AUDIO_ID;
		try {
			if (mManager != null) {
				result = mManager.getAudioId();
			}
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to get audio id", e);
		} catch (NoSuchMethodError e) {
			Dbg.e(TAG + "Failed to get audio id", e);
		}

		return result;
	}

}
