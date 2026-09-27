package com.vtstudio.fxbox.downloader;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vtstudio.fxbox.R;
import com.vtstudio.fxbox.database.FxRoomDB;
import com.vtstudio.fxbox.media.models.youtube.YTDetails;
import com.vtstudio.fxbox.media.sources.DataSourceBuilder;
import com.vtstudio.fxbox.models.TiktokPackage;
import com.vtstudio.fxbox.notifications.DownloadNotificationCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FXDownloader extends Service {

    private static final int DOWNLOAD_BUFFER_KB = 1024;
    private static volatile boolean isNetworkAvailable = true;
    private boolean bound;
    private final boolean canContinueDownloading = true;
    private Handler eventHandler;
    private ExecutorService executor;

    public class Request {
        private RequestInfo info;
        private int read;
        private Runnable downloadRunnable;
        private OnDownloadListener downloadListener;
        private OnSaveModelCallback saveModelCallback;
        private long lastProgressNotificationTime;
        private boolean isInputStreamClosed;
        private InputStream inputStream;
        private OutputStream outputStream;
        private byte[] buffer;
        private boolean isCalling;
        private boolean isCancelled;
        private ResponseBody body;
        private Request(int id) {
            this.info = new RequestInfo(id);
        }

        public Request with(@NonNull String dir, @NonNull String fileName) {
            this.info.file = new File(dir, fileName);
            this.info.fileName = fileName;
            this.info.dir = dir;
            return this;
        }

        public Request with(@NonNull File file) {
            this.info.fileName = file.getName();
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                this.info.dir = parentFile.getAbsolutePath();
                this.info.file = new File(parentFile.getAbsolutePath(), file.getName());
            }
            return this;
        }


        public Request mineType(@NonNull String mineType) {
            this.info.mineType = mineType;
            return this;
        }

        public Request url(@NonNull String url) {
            this.info.url = url;
            return this;
        }

        public Request thumbnail(@NonNull String path) {
            this.info.thumbnailPath = path;
            return this;
        }

        public Request title(@NonNull String title) {
            this.info.title = title;
            return this;
        }

        public Request clearThumbnailOnDestroy(boolean clear) {
            this.info.clearThumbnail = clear;
            return this;
        }

        public Request withModels(@NonNull ModelDownload shorts, @NonNull OnSaveModelCallback callback) {
            saveModelMap.put(this.info.id, shorts);
            this.info.saveWithModels = true;
            this.saveModelCallback = callback;
            return this;
        }

        public RequestInfo getInfo() {
            return info;
        }

        public void enqueue() {
            FXDownloader.this.enqueue(this);
        }

        /* các chính method xữ lí tải xuống
         * gồm các phương thức như sau:
         * 1. call - phương thức này sẽ kết nối đến máy chủ
         * 2. get - phuương thức này sẽ lấy dữ liệu từ phản hồi
         * 3. download - phương thức này sẽ bắt đầu tải tệp xuống
         * 4. pause - phương thức này sẽ tạm dừng quá trình tải xuống
         * 5. resume - phương thức này sẽ tiếp tục quá trình tải xuống
         * 6. release - phương thức này sẽ hủy quá trình tải xuống
         */

        // 1 call
        private void call(OkHttpClient client) {

            if (isCancelled) return;

            if (!isCalling) {
                Log.w("FxDownloader", "Cannot call request because existing request is calling, so this request has been ignored");
            }

            isCalling = true;
            downloadNotificationCreator.createNotification(info.title, info.id, 0, 0, false, false, false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(this.info.id, downloadNotificationCreator.getNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(this.info.id, downloadNotificationCreator.getNotification());
            }

            notifyOnPrepare();
            
            String userAgent = System.getProperty("http.agent");
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(info.url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", userAgent != null ? userAgent : "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36")
                    .get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("Downloader", e.getMessage());
                    notifyOnFailed();
                    isCalling = false;
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful() && get(response)) {
                        download();
                    } else {
                        response.close();
                        notifyOnFailed();
                    }
                    isCalling = false;
                }
            });
        }

        private synchronized void reCall(@NonNull OkHttpClient client) {
            if (!info.isCompleted && info.isFailed && !isDownloading) {
                info.isFailed = false;
                call(client);
            }
        }


        // 2 get
        private synchronized boolean get(@NonNull Response response) {
            ResponseBody bd = response.body();
            if (bd != null) {
                if (inputStream != null) {
                    closeBody();
                    closeInput();
                }
                isInputStreamClosed = false;
                inputStream = bd.byteStream();
                body = bd;
                if (info.currentBytes > 0) {
                    try {
                        long skipped = inputStream.skip(info.currentBytes);
                        if (skipped != info.currentBytes) {
                            throw new IllegalStateException("skipped failed in download");
                        }
                    } catch (IOException | IllegalStateException e) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException | IllegalStateException ignored) {

                            }
                        }
                        return false;
                    }
                }
            } else {
                return false;
            }
            this.info.contentLength = bd.contentLength();
            return true;
        }

        // 3 download

        @SuppressLint("ObsoleteSdkInt")
        private void download() {


            // khởi tạo luồng
            downloadRunnable = () -> {
                try {

                    if (!openOutputStreamIfNotYet()) {
                        return;
                    }

                    while (true) {

                        if (info.isPause) return;

                        read = inputStream.read(buffer);

                        if (this.read == -1) break;

                        outputStream.write(buffer, 0, read);

                        this.info.currentBytes += read;

                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - lastProgressNotificationTime;

                        // Kiểm tra thời gian giữa các lần gọi notifyOnProgress()
                        if (elapsedTime >= 500) {
                            notifyOnProgress();
                            lastProgressNotificationTime = currentTime;
                        }
                    }

                    notifyOnProgress();
                    info.isCompleted = true;
                    outputStream.flush();
                    closeOutputStreamIfNotYet();
                    notifyOnSuccess();

                } catch (IOException | IllegalStateException e) {
                    try {
                        if (inputStream != null && !isInputStreamClosed) {
                            closeBody();
                            inputStream.close();
                            isInputStreamClosed = true;
                        }
                    } catch (IOException ex) {
                        Log.d("FXDownloader", "IOException, error when close input: " + ex.getMessage());
                    }

                    Log.d("FXDownloader", "IOException, error when downloading: " + e.getMessage());
                    if (isNetworkAvailable) {
                        notifyOnInterruption();
                    }

                } finally {
                    if (info.isCompleted) {
                        closeInput();
                    }
                    if (eventHandler != null && !isCancelled) {
                        eventHandler.postDelayed(() -> {
                            if (!info.isCompleted && !info.isFailed && isNetworkAvailable && !isDownloading) {
                                notifyOnInterruption();
                            }
                        }, 1000);
                    }
                }
            };

            boolean mkdirsSuccess = this.info.file.getParentFile() != null && this.info.file.getParentFile().exists();

            if (!mkdirsSuccess) {
                mkdirsSuccess = this.info.file.getParentFile().mkdirs();
            }

            if (mkdirsSuccess) {
                initBufferBytesIfNotYet();
                downloadWithFile();
            } else {
                notifyOnFailed();
            }
        }

        private void onLostNetwork() {
            if (isCancelled) return;

            if (!info.isCompleted && !info.isPause) {
                info.isWaitingConnect = true;
                eventHandler.post(() -> {
                    if (managerListener != null) managerListener.onNetworkErrol(info);
                });
                downloadNotificationCreator.createNotification(info.title, info.id, info.currentBytes, info.contentLength, false, false, true);
            }
        }

        private void onNetworkConnect() {
            if (info.isStarted && !info.isCompleted && info.isWaitingConnect && !isCancelled) {
                info.isWaitingConnect = false;
                continueDownload();
            }
        }

        private boolean openOutputStreamIfNotYet() {

            if (outputStream != null) {
                return true;
            }

            try {
                outputStream = new FileOutputStream(this.info.file);
                return true;
            } catch (FileNotFoundException e) {
                Log.e("FXDownloader", "Cannot create output stream\n" + e.getMessage());
                notifyOnFailed();
                return false;
            }
        }

        private void closeOutputStreamIfNotYet() throws IOException {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        private void initBufferBytesIfNotYet() {
            if (buffer != null) return;

            this.buffer = new byte[2 * DOWNLOAD_BUFFER_KB];
        }

        private void downloadWithFile() {
            notifyOnStart();
            resume();
        }

        private void resume(boolean resumeAnyWay) {

            if (isCancelled || info.isCompleted) return;

            if (!resumeAnyWay) {
                if (!this.info.isStarted || (!this.info.isPause && !this.info.isWaitingConnect))
                    return;
            }
            notifyOnResume();
            this.info.isPause = false;
            Thread downloadThread = new Thread(downloadRunnable);
            downloadThread.start();
        }

        private void resume() {
            resume(false);
        }

        private void pause() {

            if (this.info.isPause || !this.info.isStarted || isCancelled) return;

            notifyOnPause();
            this.info.isPause = true;
        }

        private void cancel() {

            if (isCancelled || info.isCompleted) return;

            try {
                Log.d("FxDownloader", "Cancelling download");
                pause();
                closeBody();
                closeInput();
                info.getFile().delete();
                closeOutputStreamIfNotYet();
                isCancelled = true;
            } catch (IOException | IllegalStateException ex) {
                Log.d("FXDownloader", "IOException, error when close input: " + ex.getMessage());
            }
        }

        private void deleteFile() {
            if (this.info != null) {
                if (this.info.file != null) {
                    this.info.file.delete();
                }
            }
        }

        private void deleteThumbnail() {
            if (this.info != null) {
                if (this.info.thumbnailPath != null) {
                    File file = new File(this.info.thumbnailPath);
                    file.delete();
                }
            }
        }

        private void deleteFileAndThumbnail() {
            deleteFile();
            deleteThumbnail();
        }

        private void continueDownload() {

            if (isCancelled) return;

            notifyOnPrepare();
            okhttp3.Request request = new okhttp3.Request.Builder().url(info.url).get().build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    notifyOnFailed();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful() && get(response)) {
                        notifyOnStart();
                        resume(true);
                    } else {
                        response.close();
                        notifyOnFailed();
                    }
                }
            });
        }

        private void release() {
            this.info.isPause = true;
            this.downloadListener = null;
            this.downloadRunnable = null;
            this.info = null;
        }

        // listener method

        /**
         * Thiết lập bộ lắng nghe cho các sự kiện tải xuống.
         *
         * @param onDownloadListener Bộ lắng nghe sẽ được thiết lập.
         */
        private Request listener(@NonNull OnDownloadListener onDownloadListener) {
            this.downloadListener = onDownloadListener;
            return this;
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống bắt đầu.
         */
        private void notifyOnPrepare() {
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onPrepare(this.info);
                    }
                });
            }
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống bắt đầu.
         */
        private void notifyOnStart() {
            this.info.isStarted = true;
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onStart(this.info);
                    }
                });
            }
        }

        /**
         * Thông báo cho bộ lắng nghe về tiến trình tải xuống.
         */
        private void notifyOnProgress() {
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onProgress(Request.this.info);
                    }
                });
            }
            downloadNotificationCreator.createNotification(Request.this.info.title, Request.this.info.id, Request.this.info.currentBytes, Request.this.info.contentLength, false, false, false);
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống thất bại.
         */
        private void notifyOnFailed() {
            info.isFailed = true;
            info.isPause = true;
            info.isWaitingConnect = false;
            isDownloading = false;

            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onFailed(this.info);
                    }
                    FXDownloader.this.nextRequest();
                });
            } else {
                FXDownloader.this.nextRequest();
            }
            downloadNotificationCreator.createNotification(Request.this.info.title, Request.this.info.id, Request.this.info.currentBytes, Request.this.info.contentLength, true, false, false);
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống bị gián đoạn.
         */
        private void notifyOnInterruption() {
            info.isFailed = true;
            info.isPause = true;
            info.isWaitingConnect = false;
            isDownloading = false;

            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onInterruption(this.info);
                    }
                    FXDownloader.this.nextRequest();
                });
            } else {
                FXDownloader.this.nextRequest();
            }
            downloadNotificationCreator.createNotification(Request.this.info.title, Request.this.info.id, Request.this.info.currentBytes, Request.this.info.contentLength, true, false, false);
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống hoàn thành thành công.
         */
        private void notifyOnSuccess() {
            isDownloading = false;
//            info.isCompleted = true;
//            if (managerListener != null) {
//                eventHandler.post(() -> {
//                    managerListener.onSuccess(Request.this.info);
//                });
//            }
//            downloadNotificationCreator.createNotification(Request.this.info.title, Request.this.info.id,
//                    Request.this.info.currentBytes, Request.this.info.contentLength, false, true, false);
            if (eventHandler != null) {
                if (this.info.saveWithModels) {
                    eventHandler.post(() -> {
                        FXDownloader.this.saveModels(Request.this);
                    });
                } else {
                    eventHandler.post(FXDownloader.this::nextRequest);
                }
            }
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống bị tạm dừng.
         */
        private void notifyOnPause() {
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onPause(this.info);
                    }
                });
            }
        }

        /**
         * Thông báo cho bộ lắng nghe khi quá trình tải xuống được tiếp tục.
         */
        private void notifyOnResume() {
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onResume(this.info);
                    }
                });
            }
        }

        private void notifyOnCancel() {
            isDownloading = false;
            if (eventHandler != null) {
                eventHandler.post(() -> {
                    if (managerListener != null) {
                        managerListener.onCancel(this.info);
                    }
                });
            }
            nextRequest();
        }

        private void closeBody () {
            if(body != null) {
                body.close();
            }
        }
        private void closeInput() {

            if (isInputStreamClosed || inputStream == null) return;

            isInputStreamClosed = true;

            try {
                inputStream.close();
            } catch (IOException | IllegalStateException ex) {
                Log.d("FXDownloader", "IOException, error when close input: " + ex.getMessage());
            }
            inputStream = null;
            buffer = null;
        }

        // features
        private void eraseIO() {
            this.info.file.delete();
            inputStream = null;
        }
    }

    private NotificationManager notificationManager;
    private DownloadNotificationCreator downloadNotificationCreator;
    private OkHttpClient okHttpClient;
    private Queue<Request> requestQueue;
    private List<RequestInfo> requestInfoList;
    private Map<Integer, ModelDownload> saveModelMap;

    private Request current;
    private int currentRequestId = 0;
    private int idCount;
    private volatile boolean isDownloading = false;
    private boolean variablesInitialized = false;
    private OnDownloadListener managerListener;
    private FxRoomDB database;

    public Map<Integer, ModelDownload> getSaveModelMap() {
        return saveModelMap;
    }

    public List<RequestInfo> getRequestInfoList() {
        return requestInfoList;
    }

    private ConnectivityManager.NetworkCallback networkCallback;

    private void createNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isNetworkAvailable = true;
                if (current != null) {
                    current.onNetworkConnect();
                }
            }

            @Override
            public void onLost(Network network) {
                isNetworkAvailable = false;
                if (current != null) {
                    current.onLostNetwork();
                    Log.d("FXDownloader", "Network Lost has been called");
                }
            }
        };
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        bound = false;
        managerListener = null;
        if (checkDownloaded()) {
            stopForeground(false);
            stopSelf();
        }
        return true;
    }

    private boolean checkDownloaded() {
        for (RequestInfo info : requestInfoList) {
            if (!info.isCompleted) return false;
        }
        return true;
    }

    public int getCurrentRequestId() {
        return currentRequestId;
    }

    private IBinder binder = new FxDownloadBinder(this);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        bound = true;
        return binder;
    }

    // Khai báo lớp bên trong bằng cách mở rộng Binder
    public static class FxDownloadBinder extends Binder {
        private final WeakReference<FXDownloader> service;

        public FxDownloadBinder(FXDownloader service) {
            this.service = new WeakReference<FXDownloader>(service);
        }

        public FXDownloader getService() {
            return service.get();
        }
    }

    @Override
    public void onRebind(Intent intent) {
        bound = true;
    }

    @Override
    public void onCreate() {
        initLists();
    }

    private void initLists() {
        // Khởi tạo dịch vụ
        requestQueue = new LinkedList<>();
        requestInfoList = new ArrayList<>();
        saveModelMap = new HashMap<>();
        createChannel();
    }

    private void initVars() {
        downloadNotificationCreator = new DownloadNotificationCreator(this);
        okHttpClient = new OkHttpClient();
        database = FxRoomDB.get(this);
        eventHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        createNetworkCallback();
        registerNetworkCallback();
        variablesInitialized = true;
    }


    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(DownloadNotificationCreator.CHANNEL_ID,
                getString(R.string.download_title), NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Request newRequest() {
        return new Request(++idCount);
    }

    public void pause() {
        if (current != null) current.pause();
    }

    public void resume() {
        if (current != null) current.resume();
        else Log.d("FxDownload", "current is null");
    }

    public void cancel(int id) {

        if (current != null && current.info.getId() == id) {
            cancel(current);
            current = null;
            return;
        }

        if (current != null) {
            Log.d("FxDownload", "current: " + current.info.getId() + " id: " + id);
        }

        if (requestInfoList != null) {
            Log.d("FxDownloader", "Cancelling: " + id);
            List<Request> requests = requestQueue.stream().filter(request -> request.info.id == id).collect(Collectors.toList());

            if (!requests.isEmpty()) {
                Log.d("FxDownloader", "Cancelling list of requests");
                for (Request request : requests) {
                    cancel(request);
                }
            } else {
                if(requestInfoList != null) {
                    requestInfoList.removeIf(info -> info.id == id);
                    notifyOnDataChanged();
                }
            }
        }
    }

    private void notifyOnDataChanged() {
        if(managerListener != null) {
            managerListener.onDataChanged();
        }
    }

    public void cancel(Request request) {
        boolean assignNull = false;
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
            assignNull = true;
        }

        executor.execute(() -> {
            if (request != null) {
                if (requestQueue != null) requestQueue.remove(request);
                if(requestInfoList != null) requestInfoList.remove(request.info);
                request.cancel();
                request.notifyOnCancel();
                if (notificationManager != null) notificationManager.cancelAll();
            }
        });

        if (assignNull) {
            executor.shutdown();
            executor = null;
        }
    }

    public void reCall() {
        if (current != null) current.reCall(okHttpClient);
    }

    private void enqueue(Request request) {
        if (!variablesInitialized) {
            initVars();
        }
        requestInfoList.add(0, request.info);
        requestQueue.offer(request);
        nextRequest();
    }

    private void saveModels(Request request) {
        if (executor != null) {
            executor.execute(() -> {
                RequestInfo info = request.info;
                boolean success = request.saveModelCallback.onSaveModel(this, request);
                if (success) {
                    info.isCompleted = true;
                    eventHandler.post(() -> {
                        if (managerListener != null) {
                            managerListener.onSuccess(info);
                        }
                    });

                    eventHandler.post(() -> downloadNotificationCreator.createNotification(info.title, info.id,
                            info.currentBytes, info.contentLength,
                            false, true, false));
                    nextRequest();
                } else {
                    request.notifyOnFailed();
                }
            });
        }
    }

    @Override
    public void onDestroy() {

        for (RequestInfo info : requestInfoList) {
            if (info.clearThumbnail) {
                File videoCache = new File(info.thumbnailPath);
                videoCache.delete();
            }
        }

        binder = null;

        if (variablesInitialized) {
            downloadNotificationCreator.createNotification(getString(R.string.clear_download_item_cache), 0, 9, 9, false, false, false);
            downloadNotificationCreator.release();
            downloadNotificationCreator = null;
            okHttpClient = null;
            current = null;
            eventHandler = null;
            try {
                DataSourceBuilder.killInstance();
            } catch (IOException ex) {
                Log.d("FxDownloader", ex.getMessage());
            }
        }

        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }

        notificationManager = null;
        requestQueue = null;
        saveModelMap = null;
        requestInfoList = null;
        managerListener = null;

        super.onDestroy();
    }

    public void setDownloadListener(OnDownloadListener onDownloadListener) {
        this.managerListener = onDownloadListener;
    }

    private synchronized void nextRequest() {
        if (!isDownloading) {
            if (!requestQueue.isEmpty()) {
                current = requestQueue.poll();
                if (current != null) {
                    Log.d("FxDownload", "RequestQueue, nextRequest:" + current.info.getId());
                    if (current.info.saveWithModels) {
                        ModelDownload model = saveModelMap.get(current.info.id);
                        if (model instanceof TiktokPackage) {
                            if (database.shortsVideoDao().isExistsByAwemeId(((TiktokPackage) model).getAwemeId())) {
                                current.notifyOnPrepare();
                                current.notifyOnStart();
                                current.notifyOnSuccess();
                                return;
                            }
                        }

                        if (model instanceof YTDetails) {
                            if (database.ytvideoDao().isExistsById((((YTDetails) model).getVideo()).getId())) {
                                current.notifyOnPrepare();
                                current.notifyOnStart();
                                current.notifyOnSuccess();
                                return;
                            }
                        }
                    }
                    currentRequestId = current.info.id;
                    isDownloading = true;
                    current.call(okHttpClient);
                }
            } else {
                stopForeground(false);
                if (!bound) {
                    eventHandler.postDelayed(this::stopSelf, 3000);
                }
            }
        }
    }
}