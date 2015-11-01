package skykai.github.rxandroiddemo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;
import skykai.github.rxandroiddemo.R;

/**
 * Created by daqingkai on 2015/11/1.
 */
public class SecondActivity extends AppCompatActivity {

    public static final String TAG = SecondActivity.class.getSimpleName();
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

//        deferSample();
//        timerSample();
//        intervalSample();
//        rangeSample();
//        repeatSample();
//        repeatWhenSample();
//        bufferSample();
//        flatMapSample();
//        flatConcatAndSwitchSample();
//        groupSample();
//        mapSample();
//        filterSample();
        zipSample();
    }

    //just操作符是在创建Observable就进行了赋值操作，而defer是在订阅者订阅时才创建Observable，
    //此时才进行真正的赋值操作
    private void deferSample() {
        i = 10;
        Observable justObservable = Observable.just(i);
        i = 12;
        Observable deferObservable = Observable.defer(new Func0<Observable<Object>>() {
            @Override
            public Observable call() {
                return Observable.just(i);
            }
        });
        i = 15;

        justObservable.subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "just completed");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {
                Log.d(TAG, "just result : " + o.toString());
            }
        });

        deferObservable.subscribe(new Subscriber() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "defer completed");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {
                Log.d(TAG, "defer result : " + o.toString());
            }
        });
    }

    //一种是隔一段时间产生一个数字，然后就结束，可以理解为延迟产生数字
    private void timerSample() {
        Observable.timer(2, TimeUnit.SECONDS).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "timer complete");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "timer error : " + e.getMessage());
            }

            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, "timer Next :" + aLong.toString());
            }
        });
    }

    //一种是每隔一段时间就产生一个数字，没有结束符，也就是是可以产生无限个连续的数字，
    private void intervalSample() {
        Observable.interval(2, 2, TimeUnit.SECONDS).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "interval complete");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "interval error : " + e.getMessage());
            }

            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, "interval Next :" + aLong.toString());
            }
        });
    }

    //range操作符是创建一组在从n开始，个数为m的连续数字，比如range(3,10)，就是创建3、4、5…12的一组数字，
    private void rangeSample() {
        Observable.range(2, 10).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "range complete");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "range error : " + e.getMessage());
            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "range Next : " + integer.toString());
            }
        });
    }

    //repeat/repeatWhen操作符
    //repeat操作符是对某一个Observable，重复产生多次结果，
    //repeatWhen操作符是对某一个Observable，有条件地重新订阅从而产生多次结果
    //repeat和repeatWhen操作符默认情况下是运行在一个新线程上的，当然你可以通过
    //传入参数来修改其运行的线程。
    private void repeatSample() {
        Observable.range(3, 3).repeat(2).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "repeat complete");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.d(TAG, "repeat Next : " + integer.toString());
            }
        });
    }

    private void repeatWhenSample() {
        Observable.just(1, 2, 3).repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Void> observable) {
                //重复3次
                return observable.zipWith(Observable.range(1, 3), new Func2<Void, Integer, Integer>() {
                    @Override
                    public Integer call(Void aVoid, Integer integer) {
                        return integer;
                    }
                }).flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<?> call(Integer integer) {
                        System.out.println("delay repeat the " + integer + " count");
                        //1秒钟重复一次
                        return Observable.timer(1, TimeUnit.SECONDS);
                    }
                });
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
            }

            @Override
            public void onNext(Integer value) {
                System.out.println("Next:" + value);
            }
        });
    }

    //buffer操作符
    //buffer操作符周期性地收集源Observable产生的结果到列表中，
    //并把这个列表提交给订阅者，订阅者处理后，清空buffer列表，
    //同时接收下一次收集的结果并提交给订阅者，周而复始。
    //需要注意的是，一旦源Observable在产生结果的过程中出现异常，
    //即使buffer已经存在收集到的结果，订阅者也会马上收到这个异常，并结束整个过程。

    private void bufferSample() {
        //定义邮件内容
        final String[] mails = new String[]{"Here is an email!", "Another email!", "Yet another email!"};
        //每隔1秒就随机发布一封邮件
        Observable<String> endlessMail = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if (subscriber.isUnsubscribed()) return;
                    Random random = new Random();
                    while (true) {
                        String mail = mails[random.nextInt(mails.length)];
                        subscriber.onNext(mail);
                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        }).subscribeOn(Schedulers.io());
        //把上面产生的邮件内容缓存到列表中，并每隔3秒通知订阅者
        endlessMail.buffer(3, TimeUnit.SECONDS).subscribe(new Action1<List<String>>() {
            @Override
            public void call(List<String> list) {

                System.out.println(String.format("You've got %d new messages!  Here they are!", list.size()));
                for (int i = 0; i < list.size(); i++)
                    System.out.println("**" + list.get(i).toString());
            }
        });
    }

    //flatMap操作符
    //flatMap操作符是把Observable产生的结果转换成多个Observable，
    // 然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。

    //flatMap操作符通过传入一个函数作为参数转换源Observable，
    // 在这个函数中，你可以自定义转换规则，最后在这个函数中返回一个新的Observable，
    // 然后flatMap操作符通过合并这些Observable结果成一个Observable，并依次提交结果给订阅者。

    //值得注意的是，flatMap操作符在合并Observable结果时，有可能存在交叉的情况。

    private void flatMapSample() {
        Observable.just(getApplicationContext().getExternalCacheDir())
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        //参数file是just操作符产生的结果，这里判断file是不是目录文件，如果是目录文件，
                        //则递归查找其子文件flatMap操作符神奇的地方在于，返回的结果还是一个Observable，
                        //而这个Observable其实是包含多个文件的Observable的，输出应该是ExternalCacheDir下的所有文件
                        return listFiles(file);
                    }
                })
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        System.out.println(file.getAbsolutePath());
                    }
                });
    }

    private Observable<File> listFiles(File f) {
        if (f.isDirectory()) {
            return Observable.from(f.listFiles()).flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    //TODO file or f ?
                    return listFiles(file);
                }
            });
        } else {
            return Observable.just(f);
        }
    }

    //concatMap操作符

    //cancatMap操作符与flatMap操作符类似，都是把Observable产生的结果转换成多个Observable，
    //然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。

    //与flatMap操作符不同的是，concatMap操作符在处理产生的Observable时，
    // 采用的是“连接(concat)”的方式，而不是“合并(merge)”的方式，这就能保证产生结果的顺序性，
    // 也就是说提交给订阅者的结果是按照顺序提交的，不会存在交叉的情况。


    //switchMap操作符

    //switchMap操作符与flatMap操作符类似，都是把Observable产生的结果转换成多个Observable，
    // 然后把这多个Observable“扁平化”成一个Observable，并依次提交产生的结果给订阅者。

    //与flatMap操作符不同的是，switchMap操作符会保存最新的Observable产生的结果而舍弃旧的结果，
    //举个例子来说，比如源Observable产生A、B、C三个结果，通过switchMap的自定义映射规则，映射后
    // 应该会产生A1、A2、B1、B2、C1、C2，但是在产生B2的同时，C1已经产生了，这样最后的结果就变成A1、A2、B1、C1、C2，B2被舍弃掉了！

    private void flatConcatAndSwitchSample() {

        //flatMap操作符的运行结果
        Observable.just(10, 20, 30).flatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10)
                    delay = 180;

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("flatMap Next:" + integer);
            }
        });

        //concatMap操作符的运行结果
        Observable.just(10, 20, 30).concatMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10)
                    delay = 180;

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("concatMap Next:" + integer);
            }
        });

        //switchMap操作符的运行结果
        Observable.just(10, 20, 30).switchMap(new Func1<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Integer integer) {
                //10的延迟执行时间为200毫秒、20和30的延迟执行时间为180毫秒
                int delay = 200;
                if (integer > 10)
                    delay = 180;

                return Observable.from(new Integer[]{integer, integer / 2}).delay(delay, TimeUnit.MILLISECONDS);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("switchMap Next:" + integer);
            }
        });
    }


    //groupBy操作符

    //groupBy操作符是对源Observable产生的结果进行分组，形成一个类型为GroupedObservable的结果集，
    //GroupedObservable中存在一个方法为getKey()，可以通过该方法获取结果集的Key值（类似于HashMap的key)。

    //值得注意的是，由于结果集中的GroupedObservable是把分组结果缓存起来，如果对每一个GroupedObservable不进行处理
    //既不订阅执行也不对其进行别的操作符运算），就有可能出现内存泄露。因此，如果你对某个GroupedObservable不进行处理，最好是对其使用操作符take(0)处理。

    private void groupSample() {
        Observable.interval(1, TimeUnit.SECONDS).take(10).groupBy(new Func1<Long, Long>() {
            @Override
            public Long call(Long value) {
                //按照key为0,1,2分为3组
                return value % 3;
            }
        }).subscribe(new Action1<GroupedObservable<Long, Long>>() {
            @Override
            public void call(final GroupedObservable<Long, Long> result) {
                result.subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long value) {
                        System.out.println("key:" + result.getKey() + ", value:" + value);
                    }
                });
            }
        });
    }

    //map操作符

    //map操作符是把源Observable产生的结果，通过映射规则转换成另一个结果集，并提交给订阅者进行处理。
    private void mapSample() {
        Observable.just(1, 2, 3, 4, 5, 6).map(new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer integer) {
                //对源Observable产生的结果，都统一乘以3处理
                return integer * 3;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println("next:" + integer);
            }
        });
    }

    //filter操作符

    //filter操作符是对源Observable产生的结果按照指定条件进行过滤，只有满足条件的结果才会提交给订阅者
    private void filterSample() {
        Observable.just(1, 2, 3, 4, 5)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer item) {
                        return (item < 4);
                    }
                }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onNext(Integer item) {
                System.out.println("Next: " + item);
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("Error: " + error.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
            }
        });
    }


    //zip操作符

    //zip操作符是把两个observable提交的结果，严格按照顺序进行合并，
    private void zipSample() {
        Observable<Integer> observable1 = Observable.just(10,20,30);
        Observable<Integer> observable2 = Observable.just(4, 8, 12, 16);
        Observable.zip(observable1, observable2, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {
                return integer + integer2;
            }
        }).subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
            }

            @Override
            public void onNext(Integer value) {
                System.out.println("Next:" + value);
            }
        });
    }

    //join操作符

    //join操作符把类似于combineLatest操作符，也是两个Observable产生的结果进行合并，
    //合并的结果组成一个新的Observable，但是join操作符可以控制每个Observable产生结果的生命周期，
    //在每个结果的生命周期内，可以与另一个Observable产生的结果按照一定的规则进行合并
    private void joinSample() {
        //产生0,5,10,15,20数列
        Observable<Long> observable1 = Observable.timer(0, 1000, TimeUnit.MILLISECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return aLong * 5;
                    }
                }).take(5);

        //产生0,10,20,30,40数列
        Observable<Long> observable2 = Observable.timer(500, 1000, TimeUnit.MILLISECONDS)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return aLong * 10;
                    }
                }).take(5);

        observable1.join(observable2, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                //使Observable延迟600毫秒执行
                return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
            }
        }, new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                //使Observable延迟600毫秒执行
                return Observable.just(aLong).delay(600, TimeUnit.MILLISECONDS);
            }
        }, new Func2<Long, Long, Long>() {
            @Override
            public Long call(Long aLong, Long aLong2) {
                return aLong + aLong2;
            }
        }).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                System.out.println("Sequence complete.");
            }

            @Override
            public void onError(Throwable e) {
                System.err.println("Error: " + e.getMessage());
            }

            @Override
            public void onNext(Long aLong) {
                System.out.println("Next: " + aLong);
            }
        });
    }
}
