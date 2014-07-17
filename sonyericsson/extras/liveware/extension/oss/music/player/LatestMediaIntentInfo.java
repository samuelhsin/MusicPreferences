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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * The latest media intent info stores the latest media intent information in
 * the preferences so that a widget or control can present correct information
 * when started. This is needed since the music extension service is only
 * running when a widget or control is visible,
 * @see com.sonyericsson.extras.liveware.extension.oss.music.MusicService#keepRunningWhenConnected()
 */
public class LatestMediaIntentInfo {

    private static final String TRACK = "track";

    private static final String ARTIST = "artist";

    private static final String AUDIO_ID = "audio_id";

    private static final String IS_PLAYING = "is_playing";

    private static final String ALBUM_ID = "album_id";

    /**
     * Set latest media intent info.
     *
     * @param context The context.
     * @param info The info to set.
     */
    public static void set(final Context context, final PlaybackInfo info) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        if (info == null) {
            throw new IllegalArgumentException("info == null");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = preferences.edit();
        editor.putString(ARTIST, info.mArtist);
        editor.putLong(AUDIO_ID, info.mAudioId);
        if (info.mIsPlaying != null) {
            editor.putBoolean(IS_PLAYING, info.mIsPlaying);
        }
        editor.putString(TRACK, info.mTrack);
        editor.putLong(ALBUM_ID, info.mAlbumId);
        editor.commit();
    }

    /**
     * Clear latest media intent info.
     *
     * @param context The context.
     */
    public static void clear(final Context context) {
        PlaybackInfo info = new PlaybackInfo();
        info.mArtist = null;
        info.mAudioId = PlaybackService.INVALID_AUDIO_ID;
        info.mIsPlaying = false;
        info.mTrack = null;

        LatestMediaIntentInfo.set(context, info);
    }

    /**
     * Get artist name from latest media intent info.
     *
     * @param context The context.
     * @return The artist name.
     */
    public static String getArtist(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString(ARTIST, null);
    }

    /**
     * Get track name from latest media intent info.
     *
     * @param context The context.
     * @return The track name.
     */
    public static String getTrack(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString(TRACK, null);
    }

    /**
     * Get audio id from latest media intent info.
     *
     * @param context The context.
     * @return The audio id.
     */
    public static long getAudioId(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getLong(AUDIO_ID, PlaybackService.INVALID_AUDIO_ID);
    }

    /**
     * Get is playing status from latest media intent info.
     *
     * @param context The context.
     * @return The is playing status.
     */
    public static boolean getIsPlaying(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getBoolean(IS_PLAYING, false);
    }

    /**
     * Get the album id from the latest media intent info.
     *
     * @param context The context.
     * @return The album id.
     */
    public static long getAlbumId(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getLong(ALBUM_ID, PlaybackService.INVALID_ALBUM_ID);
    }


}
