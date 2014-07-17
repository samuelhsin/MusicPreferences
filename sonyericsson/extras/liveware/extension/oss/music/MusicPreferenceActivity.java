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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sonyericsson.extras.liveware.extension.oss.music.player.LatestMediaIntentInfo;
import com.sonyericsson.extras.liveware.extension.oss.music.player.MediaPlayerAdapter;
import com.sonyericsson.extras.liveware.extension.util.Dbg;

/**
 * The music preference activity handles the preferences for the music
 * extension.
 */
public class MusicPreferenceActivity extends PreferenceActivity {

	private static final int DIALOG_SHOW_LICENSE = 1;

	private OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (key.equals(getString(R.string.preference_music_player_key))) {
				Dbg.d("Music player changed. Clear latest info.");
				LatestMediaIntentInfo.clear(MusicPreferenceActivity.this);

				Intent mediaUpdateIntent = new Intent(MediaPlayerAdapter.ACTION_MEDIA_UPDATE);
				MusicPreferenceActivity.this.sendBroadcast(mediaUpdateIntent);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(mListener);

		addPreferencesFromResource(R.xml.preferences);

		Intent intent = getIntent();

		// Retrieve the URI from the intent, this is a URI to a MediaStore audio
		// file
		if (intent != null) {
			Uri trackUri = intent.getData();

			// Use it to query the media provider
			ContentResolver resolver = getContentResolver();
			if (resolver != null && trackUri != null) {
				Cursor trackCursor = resolver.query(trackUri, new String[] { MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM }, null, null, null);

				if (trackCursor != null) {
					try {
						if (trackCursor.moveToFirst()) {

							// And retrieve the wanted information
							String trackName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
							String albumName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
							String artistName = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

						}
					} finally {
						trackCursor.close();
					}
				}
			}
		}

	}

	@Override
	protected void onDestroy() {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.unregisterOnSharedPreferenceChangeListener(mListener);

		super.onDestroy();
	}

	/**
	 * Create show license dialog
	 * 
	 * @return The Dialog
	 */
	private Dialog createShowLicenseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(R.string.preferences_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		View view = LayoutInflater.from(this).inflate(R.layout.oss_license_dialog_content, null);
		TextView licenseTextView = (TextView) view.findViewById(R.id.license_text);
		licenseTextView.setText(getLicenseText());
		builder.setView(view);
		return builder.create();
	}

	/**
	 * Read raw license file into a String
	 * 
	 * @return String with the license text
	 */
	private String getLicenseText() {
		InputStream inputStream = null;
		BufferedReader reader = null;
		StringBuffer text = new StringBuffer();
		try {
			inputStream = getResources().openRawResource(R.raw.licensetext);
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = reader.readLine();
			while (null != line) {
				text.append(line);
				text.append("\n");
				line = reader.readLine();
			}
		} catch (IOException e) {
			Dbg.e("Failed to read license text from resource file. ", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignore) {
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ignore) {
			}
		}
		return text.toString();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preferences_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.oss_license_menu:
			showDialog(DIALOG_SHOW_LICENSE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_SHOW_LICENSE:
			dialog = createShowLicenseDialog();
			break;
		default:
			// Not a valid dialog
			break;
		}
		return dialog;
	}

}
