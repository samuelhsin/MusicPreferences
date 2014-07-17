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

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.text.TextPaint;
import android.util.DisplayMetrics;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.oss.music.player.MediaPlayerAdapter;
import com.sonyericsson.extras.liveware.extension.oss.music.player.PlaybackListener;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

/**
 * The music control extension allows you to control a music player from an
 * accessory.
 */
public class MusicControlExtension extends ControlExtension implements PlaybackListener {

	public static final int WIDTH = 128;

	public static final int HEIGHT = 128;

	private static final int STATE_IDLE = 0;

	private static final int STATE_STARTED = 1;

	private static final int STATE_PAUSED = 2;

	private static final int TITLE_Y_POS = HEIGHT - 7;

	private static final int TITLE_WIDTH = WIDTH - 2 * 24;

	private static final int ARTIST_Y_POS = TITLE_Y_POS - 19;

	private static final int ARTIST_WIDTH = WIDTH - 2 * 6;

	private static final Rect VOLUME_RECT = new Rect(25, 11, WIDTH - 25, 11 + 6);

	private static final int PLAY_PAUSE_X = 39;

	private static final int PLAY_PAUSE_Y = 39;

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

	private int mState = STATE_IDLE;

	private ArrayList<ControlButton> mButtons = new ArrayList<ControlButton>();

	private MediaPlayerAdapter mMediaPlayerAdapter;

	private Bitmap mBitmap;

	private final AudioManager mAudioManager;

	private PlayPauseButton mPlayPauseButton = null;

	private String mCurrentArtist = null;

	private String mCurrentTitle = null;

	private Bitmap mCurrentAlbumArt = null;

	private boolean mCurrentIsPlaying = false;

	private int mCurrentVolume = -1;

	/**
	 * Create music control extension.
	 * 
	 * @param context
	 *            The context to use.
	 * @param packageName
	 *            The package name of the host application.
	 */
	public MusicControlExtension(final Context context, final String packageName) {
		super(context, packageName);

		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public void onStart() {
		Dbg.d("onStart");

		mState = STATE_PAUSED;

		mMediaPlayerAdapter = new MediaPlayerAdapter(mContext);

		mCurrentArtist = null;
		mCurrentTitle = null;
		mCurrentAlbumArt = null;
		mCurrentIsPlaying = false;
		mCurrentVolume = -1;

		// Show info about current track on screen
		createButtons();

		// Start listening for play back updates
		mMediaPlayerAdapter.startAndBindToMediaService();
	}

	@Override
	public void onStop() {
		Dbg.d("onStop");

		mState = STATE_IDLE;

		// Clear current bitmap.
		mBitmap = null;

		if (mMediaPlayerAdapter != null) {
			// Player updates no longer needed.
			mMediaPlayerAdapter.onDestroy();
			mMediaPlayerAdapter = null;
		}
	}

	@Override
	public void onPause() {
		Dbg.d("onPause");

		mState = STATE_PAUSED;

		// Player updates no longer needed.
		mMediaPlayerAdapter.unregisterListener(this);
	}

	@Override
	public void onResume() {
		Dbg.d("onResume");

		mState = STATE_STARTED;
		mCurrentArtist = null;
		mCurrentTitle = null;
		mCurrentAlbumArt = null;
		mCurrentIsPlaying = false;
		mCurrentVolume = -1;

		// Re-register for player updates.
		mMediaPlayerAdapter.registerListener(this);

		// Update the display with the latest info.
		updateDisplay(true);
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayerAdapter != null) {
			// Player updates no longer needed.
			mMediaPlayerAdapter.unregisterListener(this);
			mMediaPlayerAdapter.onDestroy();
			mMediaPlayerAdapter = null;
		}
	}

	@Override
	public void onTouch(final ControlTouchEvent event) {
		Dbg.d("onTouch");

		if (event != null) {
			Dbg.v("action: " + event.getAction() + " x: " + event.getX() + " y: " + event.getY() + " time: " + event.getTimeStamp());
		}
		if (mBitmap == null) {
			// If Music Extension has stopped
			return;
		}
		// Check touch on any buttons
		for (int i = 0; i < mButtons.size(); i++) {
			ControlButton button = mButtons.get(i);
			boolean oldIsPressed = button.isPressed();
			button.checkTouchEvent(event);

			// Press status changed. Update display.
			if (button.isPressed() != oldIsPressed) {
				updateBitmapAndButton(button);
			}
		}
	}

	@Override
	public void onSwipe(int direction) {
		Dbg.d("onSwipe: " + direction);

		if (direction == Control.Intents.SWIPE_DIRECTION_RIGHT) {
			mMediaPlayerAdapter.prev();
		} else if (direction == Control.Intents.SWIPE_DIRECTION_LEFT) {
			mMediaPlayerAdapter.next();
		}
	}

	/**
	 * Update the button part of a bitmap. Used to avoid sending large images to
	 * accessory.
	 * 
	 * @param button
	 *            The button to update.
	 */
	private void updateBitmapAndButton(ControlButton button) {
		Dbg.d("updateBitmapAndButton.");

		// Get the current background.
		Bitmap bitmap = mBitmap.copy(BITMAP_CONFIG, true);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		// Draw the button
		canvas.drawBitmap(button.getBitmap(), button.getX(), button.getY(), paint);

		// Update the volume
		boolean volumeChange = showCurrentVolume(canvas);

		// Create snapshot of part of the bitmap to send.
		Bitmap outBitmap = Bitmap.createBitmap(bitmap, button.getX(), button.getY(), button.getWidth(), button.getHeight());
		showBitmap(outBitmap, button.getX(), button.getY());

		// If volume has been changed we must update the volume bar as well.
		if (volumeChange) {
			outBitmap = Bitmap.createBitmap(bitmap, VOLUME_RECT.left, VOLUME_RECT.top, VOLUME_RECT.width(), VOLUME_RECT.height());
			showBitmap(outBitmap, VOLUME_RECT.left, VOLUME_RECT.top);
		}

	}

	/**
	 * Send a complete update of the screen to the accessory.
	 */
	private void showBitmapAndButtons() {
		Dbg.d("showBitmapAndButtons");

		// Create a copy of the bitmap so that we don't keep the original
		Bitmap bitmap = mBitmap.copy(BITMAP_CONFIG, true);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		// Add all buttons.
		for (int i = 0; i < mButtons.size(); i++) {
			ControlButton button = mButtons.get(i);
			canvas.drawBitmap(button.getBitmap(), button.getX(), button.getY(), paint);
		}

		// Update the volume
		showCurrentVolume(canvas);

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
		Dbg.d("onUpdate: Playing:" + mMediaPlayerAdapter.isPlaying() + " " + mMediaPlayerAdapter.getArtist() + ": " + mMediaPlayerAdapter.getTitle());

		// Only update the screen if the control is started.
		if (mState != STATE_STARTED) {
			Dbg.w("Update received in state: " + mState);
			return;
		}

		updateDisplay(false);
	}

	/**
	 * Update the accessory display with info about the track currently being
	 * played and add all buttons on top of it.
	 * 
	 * @param forceUpdate
	 *            True if update regardless if playback info is changed.
	 */
	private void updateDisplay(boolean forceUpdate) {
		String artist = mMediaPlayerAdapter.getArtist();
		String title = mMediaPlayerAdapter.getTitle();
		Bitmap albumArt = mMediaPlayerAdapter.getAlbumArt();
		boolean isPlaying = mMediaPlayerAdapter.isPlaying();

		mPlayPauseButton.update(isPlaying);

		if (!forceUpdate && Utils.equalsNullSafe(artist, mCurrentArtist) && Utils.equalsNullSafe(title, mCurrentTitle) && Utils.equalsNullSafe(albumArt, mCurrentAlbumArt)) {
			Dbg.d("Control: Track info already up to date");

			if (isPlaying == mCurrentIsPlaying) {
				Dbg.d("Control: Playstate also up to date. Nothing to do.");
			} else {
				updateBitmapAndButton(mPlayPauseButton);
				mCurrentIsPlaying = isPlaying;
			}
			return;
		}

		mCurrentArtist = artist;
		mCurrentTitle = title;
		mCurrentAlbumArt = albumArt;
		mCurrentIsPlaying = isPlaying;

		// Create bitmap to draw in.
		mBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, BITMAP_CONFIG);

		// Set the density to default to avoid scaling.
		mBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		Canvas canvas = new Canvas(mBitmap);
		Paint paint = new Paint();

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

		// Add album art.
		if (albumArt == null) {
			albumArt = BitmapFactory.decodeResource(mContext.getResources(), albumImgIndex, mBitmapOptions);
		}
		Rect source = new Rect(0, 0, albumArt.getWidth(), albumArt.getHeight());
		Rect dest = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		canvas.drawBitmap(albumArt, source, dest, paint);

		Bitmap volumeBackground = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player_text_top_bg, mBitmapOptions);
		canvas.drawBitmap(volumeBackground, 0, 0, paint);

		// Add background for text.
		Bitmap textBackground = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.player_text_bottom_bg, mBitmapOptions);
		canvas.drawBitmap(textBackground, 0, HEIGHT - textBackground.getHeight(), paint);

		TextPaint textPaint = new TextPaint(paint);
		textPaint.setAntiAlias(true);
		textPaint.setTextAlign(Paint.Align.CENTER);

		// Add artist
		if (artist != null) {
			TextPaint artistPaint = new TextPaint(textPaint);
			artistPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.smart_watch_text_size_normal));
			artistPaint.setColor(mContext.getResources().getColor(R.color.smart_watch_text_color_white));
			int textX = WIDTH / 2;
			ExtensionUtils.drawText(canvas, artist, textX, ARTIST_Y_POS, artistPaint, ARTIST_WIDTH);
		}

		// Add title
		if (title != null) {
			TextPaint titlePaint = new TextPaint(textPaint);
			titlePaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.smart_watch_text_size_small));
			titlePaint.setColor(mContext.getResources().getColor(R.color.smart_watch_text_color_white));
			int textX = WIDTH / 2;
			ExtensionUtils.drawText(canvas, title, textX, TITLE_Y_POS, titlePaint, TITLE_WIDTH);
		}

		// Add buttons
		showBitmapAndButtons();
	}

	/**
	 * Show the current volume.
	 */
	private boolean showCurrentVolume(final Canvas canvas) {
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		Dbg.d("showCurrentVolume max:" + maxVolume + " volume:" + volume);

		int volumeOnLength = (int) (VOLUME_RECT.width() * volume / maxVolume);

		Bitmap volumeOnBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volume_on_icn, mBitmapOptions);
		Bitmap volumeOffBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volume_off_icn, mBitmapOptions);
		RectF volumeOnRect = new RectF(VOLUME_RECT.left, VOLUME_RECT.top, VOLUME_RECT.left + volumeOnLength, VOLUME_RECT.bottom);
		canvas.drawBitmap(volumeOnBitmap, null, volumeOnRect, null);

		RectF volumeOffRect = new RectF(volumeOnRect.right, VOLUME_RECT.top, VOLUME_RECT.right, VOLUME_RECT.bottom);
		canvas.drawBitmap(volumeOffBitmap, null, volumeOffRect, null);

		boolean volumeChange = (volume != mCurrentVolume);
		mCurrentVolume = volume;

		return volumeChange;
	}

	/**
	 * Create all buttons.
	 */
	private void createButtons() {
		mButtons.clear();

		// Volume down
		Bitmap volumeDownBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volme_minus_icn, mBitmapOptions);
		Bitmap volumeDownPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volme_minus_pressed_icn, mBitmapOptions);
		ControlButton volumeDownButton = new ControlButton(0, 0, volumeDownBitmap, volumeDownPressedBitmap) {
			@Override
			public void onClick() {
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
			}
		};
		mButtons.add(volumeDownButton);

		// Volume up
		Bitmap volumeUpBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volme_plus_icn, mBitmapOptions);
		Bitmap volumeUpPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_volme_plus_pressed_icn, mBitmapOptions);
		ControlButton volumeUpButton = new ControlButton(WIDTH - volumeUpBitmap.getWidth(), 0, volumeUpBitmap, volumeUpPressedBitmap) {
			@Override
			public void onClick() {
				mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
			}
		};
		mButtons.add(volumeUpButton);

		// Previous
		Bitmap previousBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_previous_icn, mBitmapOptions);
		Bitmap previousPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_previous_pressed_icn, mBitmapOptions);
		ControlButton previousButton = new ControlButton(0, HEIGHT - previousBitmap.getHeight(), previousBitmap, previousPressedBitmap) {
			@Override
			public void onClick() {
				mMediaPlayerAdapter.prev();
			}
		};
		mButtons.add(previousButton);

		// Next
		Bitmap nextBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_next_icn, mBitmapOptions);
		Bitmap nextPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_next_pressed_icn, mBitmapOptions);
		ControlButton nextButton = new ControlButton(WIDTH - nextBitmap.getWidth(), HEIGHT - nextBitmap.getHeight(), nextBitmap, nextPressedBitmap) {
			@Override
			public void onClick() {
				mMediaPlayerAdapter.next();
			}
		};
		mButtons.add(nextButton);

		// Play pause
		Bitmap playBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_play_icn, mBitmapOptions);
		Bitmap playPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_play_pressed_icn, mBitmapOptions);
		Bitmap pauseBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_pause_icn, mBitmapOptions);
		Bitmap pausePressedBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music_pause_pressed_icn, mBitmapOptions);
		mPlayPauseButton = new PlayPauseButton(PLAY_PAUSE_X, PLAY_PAUSE_Y, mMediaPlayerAdapter, playBitmap, playPressedBitmap, pauseBitmap, pausePressedBitmap);
		mButtons.add(mPlayPauseButton);
	}

}
