package skykai.github.rxandroiddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);


        firstSample();
    }

    private void firstSample() {

        // RxJava 有四个基本概念：Observable (可观察者，即被观察者)、
        // Observer (观察者)、 subscribe (订阅)、事件。Observable 和 Observer 通过 subscribe()
        // 方法实现订阅关系，从而 Observable 可以在需要的时候发出事件来通知 Observer。
        Observable<String> myObservable1 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello World");
            }
        });

        Observer<String> myObserver1 = new Observer<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG,"myObserver1 : " + s);
            }
        };
        myObservable1.subscribe(myObserver1);

        // 除了 Observer 接口之外，RxJava 还内置了一个实现了 Observer 的抽象类：Subscriber。
        // Subscriber 对 Observer 接口进行了一些扩展，但他们的基本使用方式是完全一样的：
        Subscriber<String> mySubscriber1 = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.d(TAG,"mySubscriber1 : " + s);
            }
        };

        myObservable1.subscribe(mySubscriber1);
    }
}
