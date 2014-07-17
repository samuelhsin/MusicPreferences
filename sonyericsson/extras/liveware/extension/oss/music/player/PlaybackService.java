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

import android.app.Service;

/**
 * The playback service is an abstract class to be sub classed for the different
 * playback services.
 */
public abstract class PlaybackService extends Service {

	public static final int INVALID_ALBUM_ID = -1;
	public static final int INVALID_AUDIO_ID = -1;

	/**
	 * Is the playback service playing.
	 * 
	 * @return True if playing.
	 */
	public abstract boolean isPlaying();

	/**
	 * Pause.
	 */
	public abstract void pause();

	/**
	 * Start playback.
	 */
	public abstract void play();

	/**
	 * Change to previous track.
	 */
	public abstract void prev();

	/**
	 * Change to next track.
	 */
	public abstract void next();

	/**
	 * Get the current track name.
	 * 
	 * @return The current track name.
	 */
	public abstract String getTrackName();

	/**
	 * Get the current artist name.
	 * 
	 * @return The current artist name.
	 */
	public abstract String getArtistName();

	/**
	 * Get the current album id.
	 * 
	 * @return The current album id.
	 */
	public abstract int getAlbumId();

	/**
	 * Get the current audio id.
	 * 
	 * @return The current audio id.
	 */
	public abstract int getAudioId();

	/**
	 * Bind to the playback service.
	 * 
	 * @return True if successfully bound.
	 */
	public abstract boolean bindService();

	/**
	 * Unbind from the playback service.
	 */
	public abstract void unbindService();

}
