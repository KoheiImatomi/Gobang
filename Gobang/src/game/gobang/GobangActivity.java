package game.gobang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class GobangActivity extends Activity {
	private TextView mMessage;
	private GameView mGameView;

	public enum GameMenu {
		NEW_GAME(1), UNDO(2);
		private int number;

		private GameMenu(final int n) {
			number = n;
		}

		public int number() {
			return number;
		}

		public static GameMenu valueOf(int num) {
			for (GameMenu m : values()) {
				if (m.number == num)
					return m;
			}
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, GameMenu.NEW_GAME.number(), 0, R.string.menu_newgame);
		menu.add(0, GameMenu.UNDO.number(), 0, R.string.menu_undo);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (GameMenu.valueOf(item.getItemId())) {
		case NEW_GAME:
			mGameView.getThread().initGame();
			mGameView.getThread().setState(GameView.GameState.READY, null);
			return true;
		case UNDO:
			if (mGameView.getThread().getGameState() == GameView.GameState.PLAYING)
				if (!mGameView.getThread().undo()) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							this);
					alertDialogBuilder.setMessage("これ以上戻れません");
					alertDialogBuilder.setPositiveButton("はい",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});
					alertDialogBuilder.setCancelable(true);
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}
			return true;
		}

		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.d("debug","onCreate");

		mGameView = (GameView) findViewById(R.id.view);
		mMessage = (TextView) findViewById(R.id.text);
		mGameView.setTextView(mMessage);

		if (savedInstanceState == null) {
			mGameView.getThread().setState(GameView.GameState.READY, null);
		} else {
			mGameView.getThread().restoreState(savedInstanceState);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mGameView.getThread().getGameState() == GameView.GameState.PLAYING && keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setMessage("ゲームを終了しますか？");
			alertDialogBuilder.setPositiveButton("はい",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							GobangActivity.this.finish();
						}
					});
			alertDialogBuilder.setNegativeButton("いいえ",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							
						}
					});
			alertDialogBuilder.setCancelable(true);
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGameView.destroyThread();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("debug", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		mGameView.getThread().saveState(outState);
	}
}