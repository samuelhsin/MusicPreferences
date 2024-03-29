/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.music;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;

import com.sonyericsson.extras.liveware.extension.oss.music.CommonUtil;
import com.sonyericsson.extras.liveware.extension.oss.music.R;

public class MusicUtils {

	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

	static {
		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		sBitmapOptions.inDither = false;
		sBitmapOptions.inDensity = DisplayMetrics.DENSITY_DEFAULT;
		sBitmapOptions.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
	}

	/**
	 * Get album art for specified album. You should not pass in the album id
	 * for the "unknown" album here (use -1 instead) This method always returns
	 * the default album art icon when no album art is found.
	 */
	public static Bitmap getArtwork(Context context, long song_id, long album_id) {
		return getArtwork(context, song_id, album_id, true);
	}

	/**
	 * Get album art for specified album. You should not pass in the album id
	 * for the "unknown" album here (use -1 instead)
	 */
	public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefault) {

		if (album_id < 0) {
			// This is something that is not in the database, so get the album
			// art directly
			// from the file.
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context);
			}
			return null;
		}

		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {
				// The album art thumbnail does not actually exist. Maybe the
				// user deleted it, or
				// maybe it never existed to begin with.
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;

		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException("Must specify an album or a song id");
		}

		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (IllegalStateException ex) {
		} catch (FileNotFoundException ex) {
		}
		return bm;
	}

	public static Bitmap getDefaultArtwork(Context context) {

		int albumImgIndex = 1;
		int ramdomIndex = CommonUtil.showRandomInteger(1, 6, new Random());
		switch (ramdomIndex) {
		case 1:
			albumImgIndex = R.drawable.default_music_album_1;
			break;
		case 2:
			albumImgIndex = R.drawable.default_music_album_2;
			break;
		case 3:
			albumImgIndex = R.drawable.default_music_album_3;
			break;
		case 4:
			albumImgIndex = R.drawable.default_music_album_4;
			break;
		case 5:
			albumImgIndex = R.drawable.default_music_album_5;
			break;
		case 6:
			albumImgIndex = R.drawable.default_music_album_6;
			break;
		}

		return BitmapFactory.decodeStream(context.getResources().openRawResource(albumImgIndex), null, sBitmapOptions);
	}

}
