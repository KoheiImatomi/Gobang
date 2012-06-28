package game.gobang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
	public enum Orientation {
		LANDSCAPE, PORTRAIT;
	}

	public enum GameState {
		PLAYING(0), RESULT(1), READY(2);
		private int number;

		private GameState(final int n) {
			number = n;
		}

		public int number() {
			return number;
		}

		public static GameState valueOf(int num) {
			for (GameState m : values()) {
				if (m.number == num)
					return m;
			}
			throw new IllegalArgumentException();
		}
	}

	class GameThread extends Thread {
		private SurfaceHolder mSurfaceHolder;
		private Handler mHandler;
		private Context mContext;
		private boolean mRun;
		private boolean mStop;

		private int mCanvasHeight = 1;
		private int mCanvasWidth = 1;

		private float mDropLength = 1f;

		private Paint mLinePaint;

		private Bitmap mGobanBitmap;
		private Bitmap mBlackBitmap;
		private Bitmap mWhiteBitmap;

		private Orientation mOrientation;
		private int boardPosition = 0;

		private float originX = 0;
		private float originY = 0;

		private Board mBoard;
		private Drop mTurn = Drop.NULL;
		private Drop winner = Drop.NULL;

		private GameState mGameState;

		public GameThread(SurfaceHolder surfaceHolder, Context context,
				Handler handler) {
			// get handles to some important objects
			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;

			mLinePaint = new Paint();
			mLinePaint.setAntiAlias(true);
			mLinePaint.setARGB(255, 0, 255, 0);
			mLinePaint.setTextSize(20);

			Resources res = context.getResources();
			mGobanBitmap = BitmapFactory.decodeResource(res, R.drawable.goban);
			mBlackBitmap = BitmapFactory.decodeResource(res, R.drawable.black);
			mWhiteBitmap = BitmapFactory.decodeResource(res, R.drawable.white);

			mBoard = new Board(15);
			mTurn = Drop.BLACK;

		}

		public void initGame() {
			synchronized (mSurfaceHolder) {
				mBoard = new Board(15);
				mTurn = Drop.BLACK;
			}
		}

		public synchronized void restoreState(Bundle savedState) {
			synchronized (mSurfaceHolder) {
				byte[] buf = savedState.getByteArray("BOARD");
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				try {
					ObjectInputStream ois = new ObjectInputStream(bais);
					mBoard = (Board) ois.readObject();
				} catch (StreamCorruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				mGameState = GameState.valueOf(savedState.getInt("STATE"));
				mTurn = Drop.valueOf(savedState.getInt("TURN"));
				winner = Drop.valueOf(savedState.getInt("WINNER"));
				setState(mGameState, null);
			}
		}

		public Bundle saveState(Bundle map) {
			synchronized (mSurfaceHolder) {
				if (map != null) {
					byte[] buf = null;
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(mBoard);
					} catch (IOException e) {
						e.printStackTrace();
					}
					buf = baos.toByteArray();
					map.putByteArray("BOARD", buf);
					map.putInt("STATE", mGameState.number());
					map.putInt("TURN", mTurn.number());
					map.putInt("WINNER", winner.number());
				}
			}
			return map;
		}

		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						doDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
				while (mStop) {

				}
			}
		}

		public void setRunning(boolean b) {
			mRun = b;
		}

		public void setStop(boolean b) {
			mStop = b;
		}

		public void setSurfaceSize(int width, int height) {
			// synchronized to make sure these all change atomically
			synchronized (mSurfaceHolder) {
				int length;
				if (width < height) {
					length = width;
					mOrientation = Orientation.PORTRAIT;
					boardPosition = (int) ((height - length) / 2);
					originX = (int) ((22 * length) / 1000);
					originY = originX + boardPosition;
				} else {
					length = height;
					mOrientation = Orientation.LANDSCAPE;
					boardPosition = (int) ((width - length) / 2);
					originY = (int) ((22f * length) / 1000f);
					originX = originY + boardPosition;
				}
				mCanvasWidth = length;
				mCanvasHeight = length;
				mGobanBitmap = Bitmap.createScaledBitmap(mGobanBitmap, length,
						length, true);

				mDropLength = (64f * length) / 1000f;
				if (mDropLength <= 0)
					mDropLength = 1f;
				mBlackBitmap = Bitmap.createScaledBitmap(mBlackBitmap,
						(int) mDropLength, (int) mDropLength, true);
				mWhiteBitmap = Bitmap.createScaledBitmap(mWhiteBitmap,
						(int) mDropLength, (int) mDropLength, true);

			}
		}

		private void doDraw(Canvas canvas) {
			canvas.drawColor(Color.BLACK);
			switch (mOrientation) {
			case LANDSCAPE:
				canvas.drawBitmap(mGobanBitmap, boardPosition, 0, null);
				break;
			case PORTRAIT:
				canvas.drawBitmap(mGobanBitmap, 0, boardPosition, null);
				break;
			}
			for (int x = 0; x < 15; x++) {
				for (int y = 0; y < 15; y++) {
					float dx = x * mDropLength + originX;
					float dy = y * mDropLength + originY;
					if (mBoard.getDropAt(x, y) == Drop.BLACK)
						canvas.drawBitmap(mBlackBitmap, dx, dy, null);
					else if (mBoard.getDropAt(x, y) == Drop.WHITE)
						canvas.drawBitmap(mWhiteBitmap, dx, dy, null);
				}
			}
		}

		public boolean undo() {
			boolean result = mBoard.undo();
			if (result)
				mTurn = (mTurn == Drop.BLACK) ? Drop.WHITE : Drop.BLACK;
			return result;
		}

		public GameState getGameState() {
			return this.mGameState;
		}

		public void setState(GameState state, CharSequence message) {
			synchronized (mSurfaceHolder) {
				mGameState = state;

				if (mGameState == GameState.PLAYING) {
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("text", "");
					b.putInt("viz", View.INVISIBLE);
					msg.setData(b);
					mHandler.sendMessage(msg);
				} else if (mGameState == GameState.READY) {
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					Resources res = mContext.getResources();
					CharSequence str = res.getText(R.string.state_ready);
					b.putString("text", str.toString());
					b.putInt("viz", View.VISIBLE);
					msg.setData(b);
					mHandler.sendMessage(msg);
				} else if (mGameState == GameState.RESULT) {
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					Resources res = mContext.getResources();
					CharSequence str = res.getText(R.string.state_result1);
					str = str + "\n" + res.getText(R.string.state_result2);
					str = str + winner.toString();
					b.putString("text", str.toString());
					b.putInt("viz", View.VISIBLE);
					msg.setData(b);
					mHandler.sendMessage(msg);
				}
			}
		}

		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (mGameState) {
			case PLAYING:
				if (event.getAction() == MotionEvent.ACTION_UP) {
					float tx = x - originX - mDropLength / 2;
					float ty = y - originY - mDropLength / 2;

					int indexX = (int) (tx / mDropLength);
					int indexY = (int) (ty / mDropLength);

					if (mBoard.isValidIndex(indexX, indexY)) {
						if (mBoard.isDroppableAt(indexX, indexY)) {
							mBoard.putAt(indexX, indexY, mTurn);
							if (mBoard.chechWin(mTurn)) {
								winner = mTurn;
								setState(GameState.RESULT, null);
							}

							mTurn = (mTurn == Drop.BLACK) ? Drop.WHITE
									: Drop.BLACK;
						}
					}
				}
				break;
			case READY:
				if (event.getAction() == MotionEvent.ACTION_UP)
					this.setState(GameState.PLAYING, null);
				break;
			case RESULT:
				if (event.getAction() == MotionEvent.ACTION_UP) {
					setState(GameState.READY, null);
					initGame();
				}
				break;
			}
			return true;
		}
	}

	private GameThread thread;

	private TextView mStatusText;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);

		// create thread only; it's started in surfaceCreated()
		thread = new GameThread(holder, context, new Handler() {
			@Override
			public void handleMessage(Message m) {
				mStatusText.setVisibility(m.getData().getInt("viz"));
				mStatusText.setText(m.getData().getString("text"));
			}
		});

		setFocusable(true); // make sure we get key events
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return thread.onTouchEvent(event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!thread.isAlive()) {
			startThread();
		} else {
			thread.setStop(false);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopThread();
	}

	public void stopThread() {
		thread.setStop(true);
	}

	public void destroyThread() {
		boolean retry = true;
		thread.setRunning(false);
		thread.setStop(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public void startThread() {
		thread.setRunning(true);
		thread.setStop(false);
		thread.start();
	}

	public GameThread getThread() {
		return this.thread;
	}

	public void setTextView(TextView textView) {
		mStatusText = textView;
	}

}
