package ckobayashi.com.br.androidspeechtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class QuotesDAO {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	public QuotesDAO(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void insertQuote(String phrase, String translation, int rating) {
		this.open();

		ContentValues values = new ContentValues();
		values.put("phrase", phrase);
		values.put("translation", translation);
		values.put("rating", rating);

		database.insert("phrases", null, values);

		this.close();
	}

	public void deleteQuote(int id) {
		this.open();

		database.delete("phrases", "id = " + id, null);

		this.close();
	}

	public void deleteAll() {
		this.open();

		database.delete("phrases", null, null);

		this.close();
	}


	public void updateQuoteRating(int id, int rating) {
		this.open();

		ContentValues values = new ContentValues();
		values.put("rating", rating);

		database.update("phrases", values, "id = " + id, null);

		this.close();
	}

	public List<Phrase> getFavorites() {

		String[] allColumns = { "id", "translation", "phrase", "rating" };

		this.open();

		List<Phrase> favorites = new ArrayList<Phrase>();

		Cursor cursor = database.query("phrases", allColumns, null, null, null,
				null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Phrase phrase = cursorToQuote(cursor);
			favorites.add(phrase);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();

		this.close();

		return favorites;
	}

	private Phrase cursorToQuote(Cursor cursor) {
		Phrase phrase = new Phrase();
		phrase.setId(cursor.getInt(0));
		phrase.setTranslation(cursor.getString(1));
		phrase.setPhrase(cursor.getString(2));
		phrase.setRating(cursor.getInt(3));

		return phrase;
	}

}