package com.sonyericsson.extras.liveware.extension.oss.music.player;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.KeyEvent;

import com.android.music.IMediaPlaybackService;
import com.sonyericsson.extras.liveware.extension.util.Dbg;

@SuppressLint("InlinedApi")
public class AndroidMediaPlaybackService extends Service {

	private final static String TAG = "AndroidMediaPlaybackService";

	private IBinder binder = new ServiceBinder(this);

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void play() {
		// long eventtime = SystemClock.uptimeMillis();
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	public boolean isPlaying() {
		return LatestMediaIntentInfo.getIsPlaying(this);
	}

	public void pause() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
	}

	public void next() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_NEXT);
	}

	public void prev() {
		sendMediaButtonIntent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
	}

	public int getAlbumId() {
		return (int) LatestMediaIntentInfo.getAlbumId(this);
	}

	public int getAudioId() {
		return (int) LatestMediaIntentInfo.getAudioId(this);
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
			sendOrderedBroadcast(downIntent, null);

			// Send up.
			Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0);
			upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
			sendOrderedBroadcast(upIntent, null);
		} catch (Exception e) {
			Dbg.e(TAG + "Failed to send media button intent.", e);
		}
	}

	public class ServiceBinder extends IMediaPlaybackService.Stub {

		WeakReference<AndroidMediaPlaybackService> mService;

		public ServiceBinder(AndroidMediaPlaybackService service) {
			mService = new WeakReference<AndroidMediaPlaybackService>(service);
		}

		@Override
		public void play() throws RemoteException {
			mService.get().play();
		}

		@Override
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}

		@Override
		public int getQueuePosition() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isPlaying() throws RemoteException {
			return mService.get().isPlaying();
		}

		@Override
		public void stop() throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void pause() throws RemoteException {
			mService.get().pause();
		}

		@Override
		public void prev() throws RemoteException {
			mService.get().prev();
		}

		@Override
		public void next() throws RemoteException {
			mService.get().next();
		}

		@Override
		public long duration() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long position() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long seek(long pos) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getTrackName() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getAlbumName() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getArtistName() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void moveQueueItem(int from, int to) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setQueuePosition(int index) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public String getPath() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setShuffleMode(int shufflemode) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getShuffleMode() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int removeTracks(int first, int last) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setRepeatMode(int repeatmode) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getRepeatMode() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getMediaMountedCount() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void openFile(String path, boolean oneShot) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void openFileAsync(String path) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void open(int[] list, int position) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void enqueue(int[] list, int action) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public int removeTrack(int id) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getAlbumId() throws RemoteException {
			return mService.get().getAlbumId();
		}

		@Override
		public int getArtistId() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int[] getQueue() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getAudioId() throws RemoteException {
			return mService.get().getAudioId();
		}

	}
}
