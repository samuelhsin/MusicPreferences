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
package com.sonyericsson.extras.liveware.extension.oss.music;

import java.util.ArrayList;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.sonyericsson.extras.liveware.extension.oss.music.player.GenericPlaybackService;

/**
 * The music remote preference handles the preference for selecting which media
 * playback service that the music remote extension shall control.
 */
public class MusicRemotePreference extends ListPreference {

	private ArrayList<CharSequence> mEntries;

	private ArrayList<CharSequence> mEntryValues;

	/**
	 * Create music remote preference.
	 * 
	 * @param context
	 *            The preference.
	 */
	public MusicRemotePreference(Context context) {
		super(context);
		init(context);
	}

	/**
	 * Create music remote preference.
	 * 
	 * @param context
	 *            The context.
	 * @param attrs
	 *            The attribute set.
	 */
	public MusicRemotePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Initialize the entries and the entry values based on the currently
	 * available playback service.
	 * 
	 * @param context
	 */
	private void init(final Context context) {
		mEntries = new ArrayList<CharSequence>();
		mEntryValues = new ArrayList<CharSequence>();

		// Check if Android generic playback service is available.
		GenericPlaybackService genericPlaybackService = new GenericPlaybackService(context);
		if (genericPlaybackService.bindService()) {
			mEntries.add(context.getString(GenericPlaybackService.PREFERENCE_ENTRY));
			mEntryValues.add(context.getString(GenericPlaybackService.PREFERENCE_VALUE));
		}
		genericPlaybackService.unbindService();

		// Media button playback is always available.

		mEntries.add(context.getString(R.string.preference_music_player_automatic));
		mEntryValues.add(context.getString(R.string.preference_music_player_value_automatic));

		mEntries.add(context.getString(R.string.preference_music_player_walkman));
		mEntryValues.add(context.getString(R.string.preference_music_player_value_walkman));

		mEntries.add(context.getString(R.string.preference_music_player_kkbox));
		mEntryValues.add(context.getString(R.string.preference_music_player_value_kkbox));

		// Set the values in the super class.
		CharSequence[] entries = new CharSequence[mEntries.size()];
		CharSequence[] entryValues = new CharSequence[mEntryValues.size()];
		setEntries(mEntries.toArray(entries));
		setEntryValues(mEntryValues.toArray(entryValues));
	}

}
