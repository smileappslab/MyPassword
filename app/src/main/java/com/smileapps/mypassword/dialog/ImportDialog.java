/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.model.AsyncResult;
import com.smileapps.mypassword.model.AsyncSingleTask;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.service.Mainbinder;

/**
 * 불러오기
 *
 * @author zeus8502
 */
@SuppressWarnings("ALL")
public class ImportDialog extends ProgressDialog implements Callback {
    private Mainbinder mainbinder;
    private Handler handler = new Handler(this);

    public ImportDialog(Context context, Mainbinder mainbinder) {
        super(context);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setCancelable(false);
        this.mainbinder = mainbinder;
    }

    private String getString(int id) {
        return getContext().getString(id);
    }

    private String getString(int id, Object... obj) {
        return getContext().getString(id, obj);
    }

    /**
     * 파일불러오기 위치
     */
    private File getRootFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPassword/");
        return file;
    }

    private ArrayList<SearchResult> searchFile(File aFile) {
        ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
        File[] files = aFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".mp")) {
                    SearchResult searchResult = new SearchResult();
                    searchResult.name = file.getName();
                    searchResult.absoluteFilePath = file.getAbsolutePath();
                    searchResults.add(searchResult);
                }
            }
        }
        return searchResults;
    }

    /**
     * 검색
     */
    @Override
    public void show() {
        setMessage(getString(R.string.import_search_file));
        super.show();
        handler.sendEmptyMessageDelayed(R.id.msg_import_search_delay_500, 500);
    }

    /**
     * 파일 검색
     */
    private void searchFile() {
        new AsyncSingleTask<ArrayList<SearchResult>>() {
            @Override
            protected AsyncResult<ArrayList<SearchResult>> doInBackground(
                    AsyncResult<ArrayList<SearchResult>> asyncResult) {
                ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

                File rootFile = getRootFile();
                searchResults.addAll(searchFile(rootFile));
                searchResults.addAll(searchFile(Environment.getExternalStorageDirectory()));
                asyncResult.setData(searchResults);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<ArrayList<SearchResult>> asyncResult) {
                dismiss();
                final ArrayList<SearchResult> searchResults = asyncResult.getData();
                if (searchResults.size() == 0) {
                    // 오류 : 파일 없음
                    Builder builder = new Builder(getContext());
                    builder.setMessage(getString(R.string.import_search_failed));
                    builder.setNegativeButton(R.string.import_sure, null);
                    builder.show();
                } else {
                    // 검색 성공, 불러올 파일 선택
                    Builder builder = new Builder(getContext());
                    builder.setTitle(R.string.import_chiose_file);
                    builder.setItems(getItems(searchResults), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SearchResult searchResult = searchResults.get(which);
                            Message message = handler.obtainMessage(R.id.msg_import, searchResult);
                            message.sendToTarget();
                        }
                    });
                    builder.show();
                }
            }
        }.execute();
    }

    /**
     * 검색 결과 : String[]
     */
    private String[] getItems(ArrayList<SearchResult> searchResults) {
        String[] result = new String[searchResults.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = searchResults.get(i).name;
        }
        return result;
    }

    /** 불러오기 */
    private void importFile(final SearchResult searchResult) {
        new AsyncSingleTask<ArrayList<Password>>() {
            @Override
            protected AsyncResult<ArrayList<Password>> doInBackground(AsyncResult<ArrayList<Password>> asyncResult) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(searchResult.absoluteFilePath);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                    ArrayList<Password> passwords = new ArrayList<Password>();
                    String passwordStr = null;
                    while ((passwordStr = bufferedReader.readLine()) != null) {
                        Password password = Password.createFormJson(passwordStr);
                        passwords.add(password);
                    }
                    bufferedReader.close();
                    asyncResult.setResult(0);
                    asyncResult.setData(passwords);
                } catch (Exception e) {
                    asyncResult.setResult(-1);
                } finally {
                    close(fileInputStream);
                }
                setDelay(500);
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<ArrayList<Password>> asyncResult) {
                dismiss();
                if (asyncResult.getResult() == 0 && mainbinder != null) {
                    // 파일 불러오기
                    ArrayList<Password> passwords = asyncResult.getData();
                    for (Password password : passwords) {
                        mainbinder.insertPassword(password);
                    }

                    Builder builder = new Builder(getContext());
                    builder.setMessage(getString(R.string.import_file_successs, passwords.size()));
                    builder.setNegativeButton(R.string.import_sure, null);
                    builder.show();
                } else {
                    // 오류 : 파일 손상됨.
                    Builder builder = new Builder(getContext());
                    builder.setMessage(getString(R.string.import_file_failed));
                    builder.setNegativeButton(R.string.import_sure, null);
                    builder.show();
                }
            }
        }.execute();
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case R.id.msg_import_search_delay_500:
                searchFile();
                break;
            case R.id.msg_import:
                SearchResult searchResult = (SearchResult) msg.obj;
                setMessage(getContext().getString(R.string.import_ing, searchResult.name));
                super.show();
                importFile(searchResult);
                break;
            default:
                break;
        }
        return true;
    }

    /** 검색 결과 */
    private static class SearchResult {
        /** 파일 이름 */
        String name;
        /** 파일 경로 */
        String absoluteFilePath;
    }
}
