package com.arcsoft.MrWater_demo;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gqj3375 on 2017/4/28.
 * Modified by Mr.water on 2018/4/01
 */

public class MainActivity extends Activity implements OnClickListener {
	private static Context context;
	FaceDB mFaceDB;
	private final String TAG = this.getClass().toString();
	private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
	private static final int REQUEST_CODE_IMAGE_OP = 2;
	private static final int REQUEST_CODE_OP = 3;
	public Timer timer = new Timer();
	public int totalThreadCount = 1;
	public int runningThreadCount;
	//Write your domain name
	String path = "http://192.168.0.105:8080/WebServer/files/Name.txt";

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		try {
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try{
						download("http://192.168.0.105:8080/WebServer/files/Name.txt");
						FileReader fileReader = null;
						BufferedReader bufferedReader = null;
					 /* Read WebServer TXT */
					fileReader = new FileReader(FaceDB.mDBPath+"/Name.txt");
					File file = new File(FaceDB.mDBPath+"/Name.txt");
					bufferedReader = new BufferedReader(fileReader);
					String line1 = null;
					final String[] ary = new String[1024];//name.txt catalog
					int i = 0;
					while(true) {
						line1 = bufferedReader.readLine();
						//Your WebServer Domain Name
						downloadFile1("http://192.168.0.105:8080/WebServer/files/"+line1);
						ary[i] = line1;
						if (line1 == null) {
							break;
						}
						System.out.println("line1" + line1);
						System.out.println("line2" + ary[i]);
						i++;
					}
						for(int k = 0;k < ary.length;k++)
						{
							((Application)MainActivity.this.getApplicationContext()).mFaceDB.delete(ary[k]);//删除以便重新录入
						}
						((Application)MainActivity.this.getApplicationContext()).mFaceDB.addFace();//重新增加注册人脸

					}catch(Exception e)
					{
						System.out.println(e);
					}
				}
			},15000 , 15000);
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try{
						restartApp();//刷新后台操作
					}catch (Exception e)
					{
						System.out.println(e);
					}
				}
			},30000,30000);
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					File file = new File(FaceDB.mDBPath);
					String []filelist = file.list();
					for(int i = 0;i<filelist.length;i++)
					{
						if(filelist[i].substring(0,7)=="visitor")
						{
							deleteFile(FaceDB.mDBPath + "/" + filelist[i]);
						}
					}
				}
			},30000,108000);

		}catch(Exception e)
		{
			System.out.println(e);
		}

		this.setContentView(R.layout.main_test);
		setScreenBright(MainActivity.this,true);
		View v = this.findViewById(R.id.button2);
		v.setOnClickListener(this);

		}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_IMAGE_OP && resultCode == RESULT_OK) {
			Uri mPath = data.getData();
			String file = getPath(mPath);
			Bitmap bmp = Application.decodeImage(file);
			if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0 ) {
				Log.e(TAG, "error");
			} else {
				Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
			}
			startRegister(bmp, file);
		} else if (requestCode == REQUEST_CODE_OP) {
			Log.i(TAG, "RESULT =" + resultCode);
			if (data == null) {
				return;
			}
			Bundle bundle = data.getExtras();
			String path = bundle.getString("imagePath");
			Log.i(TAG, "path="+path);
		} else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
			Uri mPath = ((Application)(MainActivity.this.getApplicationContext())).getCaptureImage();
			String file = getPath(mPath);
			Bitmap bmp = Application.decodeImage(file);
			startRegister(bmp, file);
		}
	}

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		switch (paramView.getId()) {
			case R.id.button2:
				if( ((Application)getApplicationContext()).mFaceDB.mRegister.isEmpty() ) {
					Toast.makeText(this, "应该是还没下载好，请稍后", Toast.LENGTH_SHORT).show();
				} else {
					startDetector(1);
				}
				break;

			default:;
		}
	}

	/**
	 * @param uri
	 * @return
	 */
	private String getPath(Uri uri) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (DocumentsContract.isDocumentUri(this, uri)) {
				// ExternalStorageProvider
				if (isExternalStorageDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					if ("primary".equalsIgnoreCase(type)) {
						return Environment.getExternalStorageDirectory() + "/" + split[1];
					}

					// TODO handle non-primary volumes
				} else if (isDownloadsDocument(uri)) {

					final String id = DocumentsContract.getDocumentId(uri);
					final Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

					return getDataColumn(this, contentUri, null, null);
				} else if (isMediaDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] {
							split[1]
					};

					return getDataColumn(this, contentUri, selection, selectionArgs);
				}
			}
		}
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor actualimagecursor = this.getContentResolver().query(uri, proj, null, null, null);
		int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		actualimagecursor.moveToFirst();
		String img_path = actualimagecursor.getString(actual_image_column_index);
		String end = img_path.substring(img_path.length() - 4);
		if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
			return null;
		}
		return img_path;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param mBitmap
	 */
	private void startRegister(Bitmap mBitmap, String file) {
		Intent it = new Intent(MainActivity.this, RegisterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", file);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);
	}

	private void startDetector(int camera) {
		Intent it = new Intent(MainActivity.this, DetecterActivity.class);
		it.putExtra("Camera", camera);
		startActivityForResult(it, REQUEST_CODE_OP);
	}
	// 保持Android设备屏幕灯长亮
	public static void setScreenBright(Activity activity, boolean keepScreenOn) {
		if (keepScreenOn) {
			activity.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	public void download(final String path) {
		String str_count = "1";
		totalThreadCount = Integer.valueOf(str_count);
		new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					int code = conn.getResponseCode();
					if (code == 200) {
						int length = conn.getContentLength();
						System.out.println("file length:" + length);
						RandomAccessFile raf = new RandomAccessFile(FaceDB.mDBPath+"/"+getDownloadFileName(path), "rw");
						// 创建一个空的文件并且设置它的文件长度等于服务器上的文件长度
						raf.setLength(length);
						raf.close();

						int blockSize = length / totalThreadCount;
						System.out.println("every block size:" + blockSize);
						runningThreadCount = totalThreadCount;
						for (int threadId = 0; threadId < totalThreadCount; threadId++) {
							int startPosition = threadId * blockSize;
							int endPosition = (threadId + 1) * blockSize - 1;
							if (threadId == (totalThreadCount - 1)) {
								endPosition = length - 1;
							}
							System.out.println("thread::" + threadId + " download range:" + startPosition + "~~" + endPosition);

							new DownloadThread(threadId, startPosition, endPosition).start();
						}

					} else {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * 从网络路径获取文件名
	 *
	 * @param path 网络路径
	 * @return 文件名
	 */
	private static String getDownloadFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * 下载文件的线程
	 */
	private class DownloadThread extends Thread {

		/**
		 * 线程id
		 */
		private int threadId;

		/**
		 * 当前线程下载的起始位置
		 */
		private int startPosition;

		/**
		 * 当前线程下载的终止位置
		 */
		private int endPosition;

		/**
		 * 当前线程需要去下载的总共的字节
		 */
		private int threadTotal;

		/**
		 * 上一次下载的总共字节数
		 */
		int lastDownloadTotalSize;

		public DownloadThread(int threadId, int startPosition, int endPosition) {
			this.threadId = threadId;
			this.startPosition = startPosition;
			this.endPosition = endPosition;
			this.threadTotal = endPosition - startPosition;
		}

		@Override
		public void run() {
			System.out.println("thread:" + threadId + " begin working");
			// lest thread download it's self range data

			try {
				File finfo = new File(FaceDB.mDBPath+"/",getDownloadFileName(path)+ ".txt");
				if (finfo.exists() && finfo.length() > 0) {
					FileInputStream fis = new FileInputStream(finfo);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String lastPosition = br.readLine();
					// This thread download data before times;
					int intLastPosition = Integer.parseInt(lastPosition);
					lastDownloadTotalSize = intLastPosition - startPosition;
					startPosition = intLastPosition;
					fis.close();
				}

				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				System.out.println("begin and end:" + threadId + " range of download: " + startPosition + "~~" + endPosition);
				conn.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
				// Download Resource from server
				int code = conn.getResponseCode();
				if (code == 206) {
					InputStream is = conn.getInputStream();
					RandomAccessFile raf = new RandomAccessFile(FaceDB.mDBPath+"/" + getDownloadFileName(path), "rw");

					// vary important, position of begin to write
					raf.seek(startPosition);
					byte[] buffer = new byte[1024];
					int len = -1;
					int total = 0; // downloaded data of current thread in this times;
					while ((len = is.read(buffer)) != -1) {
						raf.write(buffer, 0, len);
						// record position of current thread to downloading
						total += len;
						RandomAccessFile inforaf = new RandomAccessFile("/storage/emulated/0/Android/data/com.arcsoft.sdk_demo/cache/" + totalThreadCount + getDownloadFileName(path) + threadId + ".txt", "rwd");
						// save position of current thread
						inforaf.write(String.valueOf(startPosition + total).getBytes());
						inforaf.close();
					}
					is.close();
					raf.close();
					System.out.println("thread:" + threadId + " download complete...");
				} else {
					System.out.println("request download failed.");
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (MainActivity.class) {
					runningThreadCount--;
					if (runningThreadCount <= 0) {
						System.out.println("multi thread download complete.");
						for (int i = 0; i < totalThreadCount; i++) {
							File finfo = new File(FaceDB.mDBPath+"/" + totalThreadCount + getDownloadFileName(path) + i + ".txt");
						}
					}
				}
			}

		}

	}
	private void downloadFile2(String url){
		//创建下载任务,downloadUrl就是下载链接
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		//指定下载路径和下载文件名
		request.setDestinationInExternalPublicDir(FaceDB.mDBPath, url.substring(url.lastIndexOf("/") + 1));
		//获取下载管理器
		DownloadManager downloadManager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		//将下载任务加入下载队列，否则不会进行下载
		downloadManager.enqueue(request);
	}
	public void downloadFile1(String url) {
		try{
			//下载路径，如果路径无效了，可换成你的下载路径
			String path = FaceDB.mDBPath;

			final long startTime = System.currentTimeMillis();
			Log.i("DOWNLOAD","startTime="+startTime);
			//下载函数
			String filename=url.substring(url.lastIndexOf("/") + 1);
			//获取文件名
			URL myURL = new URL(url);
			URLConnection conn = myURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			int fileSize = conn.getContentLength();//根据响应获取文件大小
			if (fileSize <= 0) throw new RuntimeException("无法获知文件大小 ");
			if (is == null) throw new RuntimeException("stream is null");
			File file1 = new File(path);
			if(!file1.exists()){
				file1.mkdirs();
			}
			//把数据存入路径+文件名
			FileOutputStream fos = new FileOutputStream(path+"/"+filename);
			byte buf[] = new byte[1024];
			int downLoadFileSize = 0;
			do{
				//循环读取
				int numread = is.read(buf);
				if (numread == -1)
				{
					break;
				}
				fos.write(buf, 0, numread);
				downLoadFileSize += numread;
				//更新进度条
			} while (true);

			Log.i("DOWNLOAD","download success");
			Log.i("DOWNLOAD","totalTime="+ (System.currentTimeMillis() - startTime));

			is.close();
		} catch (Exception ex) {
			Log.e("DOWNLOAD", "error: " + ex.getMessage(), ex);
		}
	}

	/**
	 * 删除单个文件
	 *
	 * @param fileName
	 *            要删除的文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				System.out.println("删除单个文件" + fileName + "成功！");
				return true;
			} else {
				System.out.println("删除单个文件" + fileName + "失败！");
				return false;
			}
		} else {
			System.out.println("删除单个文件失败：" + fileName + "不存在！");
			return false;
		}
	}
	private void restartApp() {
		final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());

	}

}

