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
package com.sonyericsson.extras.liveware.extension.oss.music;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.oss.music.player.MediaPlayerAdapter;
import com.sonyericsson.extras.liveware.extension.oss.music.player.PlaybackListener;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.SmartWatchConst;
import com.sonyericsson.extras.liveware.extension.util.widget.WidgetExtension;

/**
 * The music widget extension handles the music widget on an accessory.
 */
public class MusicWidgetExtension extends WidgetExtension implements PlaybackListener {

	private static final int PX_FRAME_BORDER = 8;

	public static final int WIDTH = 128;

	public static final int HEIGHT = 110;

	private static final int TRACK_X_OFFSET = 0;

	private static final int ARTIST_X_OFFSET = 10 + 9;

	private static final long INFO_TIMEOUT_MS = 2 * DateUtils.SECOND_IN_MILLIS;

	private final BitmapFactory.Options mBitmapOptions;

	private MediaPlayerAdapter mMediaPlayerAdapter;

	private String mCurrentArtist = null;

	private String mCurrentTitle = null;

	private Bitmap mCurrentAlbumArt = null;

	private boolean mCurrentIsPlaying = false;

	private Rect mIconRect = null;

	private final Handler mHandler = new Handler();

	private Runnable mInfoTimeout = null;

	/**
	 * Create music extension widget.
	 * 
	 * @param context
	 *            The context.
	 * @param hostAppPackageName
	 *            The host app package name for this widget.
	 */
	public MusicWidgetExtension(final Context context, final String hostAppPackageName) {
		super(context, hostAppPackageName);

		// Set some default bitmap factory options that we frequently will use.
		mBitmapOptions = new BitmapFactory.Options();
		// We use default throughout the extension to avoid any automatic
		// scaling.
		// Keep in mind that we are not showing the images on the phone, but on
		// the accessory.
		mBitmapOptions.inDensity = DisplayMetrics.DENSITY_DEFAULT;
		mBitmapOptions.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
	}

	@Override
	public void onStartRefresh() {
		// Set a info timeout. If we don't get any info from the playback
		// service then show that the playback info is not known.
		mInfoTimeout = new Runnable() {
			public void run() {
				updateWidget(true);
				mInfoTimeout = null;
			}
		};
		mHandler.postDelayed(mInfoTimeout, INFO_TIMEOUT_MS);

		// Start listening for play back updates
		if (mMediaPlayerAdapter == null) {
			mMediaPlayerAdapter = new MediaPlayerAdapter(mContext);
			mMediaPlayerAdapter.registerListener(this);
			mMediaPlayerAdapter.startAndBindToMediaService();
		}

		// Clear saved info to make sure image is updated.
		mCurrentArtist = null;
		mCurrentTitle = null;
		mCurrentAlbumArt = null;
		mCurrentIsPlaying = false;
	}

	@Override
	public void onStopRefresh() {
		if (mMediaPlayerAdapter != null) {
			// Stop listening for play back updates.
			mMediaPlayerAdapter.unregisterListener(this);
			mMediaPlayerAdapter.onDestroy();
			mMediaPlayerAdapter = null;
		}

		// Remove the info timeout.
		if (mInfoTimeout != null) {
			mHandler.removeCallbacks(mInfoTimeout);
			mInfoTimeout = null;
		}
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayerAdapter != null) {
			// Stop listening for play back updates.
			mMediaPlayerAdapter.unregisterListener(this);
			mMediaPlayerAdapter.onDestroy();
			mMediaPlayerAdapter = null;
		}
	}

	@Override
	public void onTouch(final int type, final int x, final int y) {
		Dbg.d("onTouch: " + type + " x: " + x + " y: " + y);

		if (mIconRect != null && mIconRect.contains(x, y)) {
			// Icon tapped. Toggle play state.
			if (mMediaPlayerAdapter.isPlaying()) {
				mMediaPlayerAdapter.pause();
			} else {
				mMediaPlayerAdapter.play();
			}
		} else {
			if (!SmartWatchConst.ACTIVE_WIDGET_TOUCH_AREA.contains(x, y)) {
				Dbg.d("Touch outside active area x: " + x + " y: " + y);
				return;
			}

			// Otherwise open control.
			Intent intent = new Intent(Control.Intents.CONTROL_START_REQUEST_INTENT);
			intent.putExtra(Control.Intents.EXTRA_AEA_PACKAGE_NAME, mContext.getPackageName());
			sendToHostApp(intent);
		}
	}

	/**
	 * Update widget.
	 * 
	 * @param forceUpdate
	 *            True if update regardless if playback info is changed.
	 */
	protected void updateWidget(boolean forceUpdate) {
		String artist = mMediaPlayerAdapter.getArtist();
		String title = mMediaPlayerAdapter.getTitle();
		Bitmap albumArt = mMediaPlayerAdapter.getAlbumArt();
		boolean isPlaying = mMediaPlayerAdapter.isPlaying();

		// If not forced update and we are not waiting for first info and
		// playback info is unchanged, then don't send an update to avoid
		// unnecessary flickering of the display.
		if (!forceUpdate && mInfoTimeout == null && Utils.equalsNullSafe(artist, mCurrentArtist) && Utils.equalsNullSafe(title, mCurrentTitle) && Utils.equalsNullSafe(albumArt, mCurrentAlbumArt)
				&& isPlaying == mCurrentIsPlaying) {
			Dbg.d("Widget: Track info already up to date");
			return;
		}

		// Media info received. Reset timeout.
		if (mInfoTimeout != null) {
			mHandler.removeCallbacks(mInfoTimeout);
			mInfoTimeout = null;
		}

		mCurrentArtist = artist;
		mCurrentTitle = title;
		mCurrentAlbumArt = albumArt;
		mCurrentIsPlaying = isPlaying;

		// Create bitmap to draw in.
		Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);

		// Set the density to default to avoid scaling.
		bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		// Frame
		Bitmap frameBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.widget_frame, mBitmapOptions);
		int left = (WIDTH - frameBitmap.getWidth()) / 2;
		int top = HEIGHT - frameBitmap.getHeight() - 1;
		Rect frame = new Rect(left, top, left + frameBitmap.getWidth(), top + frameBitmap.getHeight());
		canvas.drawBitmap(frameBitmap, frame.left, frame.top, paint);
		Rect innerFrame = new Rect(frame.left + PX_FRAME_BORDER, frame.top + PX_FRAME_BORDER, frame.right - PX_FRAME_BORDER, frame.bottom - PX_FRAME_BORDER);

		// AlbumArt
		if (albumArt == null) {

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

			albumArt = BitmapFactory.decodeResource(mContext.getResources(), albumImgIndex, mBitmapOptions);
		}
		Rect source = new Rect(0, 0, albumArt.getWidth(), albumArt.getHeight());
		canvas.drawBitmap(albumArt, source, innerFrame, paint);

		// Add background for text. Align it to bottom of inner frame.
		Bitmap textBackgroundBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.widget_text_3_bg, mBitmapOptions);
		int backgroundTop = innerFrame.bottom - textBackgroundBitmap.getHeight();
		canvas.drawBitmap(textBackgroundBitmap, innerFrame.left, backgroundTop, paint);

		Rect textFrame = new Rect(innerFrame.left + 3, innerFrame.top, innerFrame.right, innerFrame.bottom - 4);

		// Add play pause icon
		int iconResourceId;
		if (isPlaying) {
			iconResourceId = R.drawable.music_widget_pause_icn;
		} else {
			iconResourceId = R.drawable.music_widget_play_icn;
		}
		Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), iconResourceId, mBitmapOptions);
		mIconRect = new Rect(WIDTH - 1 - icon.getWidth(), HEIGHT - 1 - icon.getHeight(), WIDTH - 1, HEIGHT - 1);

		// Create default text paint
		TextPaint textPaint = new TextPaint(paint);
		textPaint.setAntiAlias(true);
		textPaint.setTextAlign(Paint.Align.LEFT);

		// Add song title
		if (title != null) {
			TextPaint titlePaint = new TextPaint(textPaint);
			titlePaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.smart_watch_text_size_small));
			titlePaint.setColor(mContext.getResources().getColor(R.color.smart_watch_text_color_grey));
			ExtensionUtils.drawText(canvas, title, textFrame.left, textFrame.bottom - TRACK_X_OFFSET, titlePaint, mIconRect.left - textFrame.left);
		}

		// Add artist
		if (artist != null) {
			TextPaint artistTextPaint = new TextPaint(textPaint);
			artistTextPaint.setColor(Color.WHITE);
			artistTextPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.smart_watch_text_size_normal));
			ExtensionUtils.drawText(canvas, artist, textFrame.left, textFrame.bottom - ARTIST_X_OFFSET, artistTextPaint, textFrame.right - textFrame.left);
		}

		canvas.drawBitmap(icon, mIconRect.left, mIconRect.top, paint);

		Dbg.d("Updating widget");
		showBitmap(bitmap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sonyericsson.extras.liveware.extension.oss.music.player.PlaybackListener
	 * #onUpdate()
	 */
	public void onUpdate() {
		// Update the widget
		updateWidget(false);
	}

}
