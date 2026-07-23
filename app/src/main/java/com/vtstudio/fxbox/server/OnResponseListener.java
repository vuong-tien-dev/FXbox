package com.vtstudio.fxbox.server;

public interface OnResponseListener<T> {
    void onResponse(Response<T> response);

    class Errol {
        public static final int SERVER_ERROL = 505;
        public static final int DATA_ERROL = 101;
        private final int errol;

        public Errol (int errol){
            this.errol = errol;
        }

        public int getErrol() {
            return errol;
        }
    }

    interface OnApiDestroyListener {
        void OnDestroy();
    }
}