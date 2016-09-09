package cn.lzx.scrolltotopview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Scroller;

/**
 * Project Name: ScrollToTopView
 * File Name:    ScrollWebView.java
 * ClassName:    ScrollWebView
 *
 * Description: 带滑动监听的webview.
 *
 * @author LZX
 * @date 2016年09月09日 10:57
 *
 */

public class ScrollWebView extends WebView implements SmoothScrollable
{
    public ScrollWebView(Context context)
    {
        super(context);
        mScroller = new Scroller(context);

    }

    public ScrollWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mScroller = new Scroller(context);
    }


    /**
     * 设置webView滑动监听
     */
    private OnScrollToTopListener mScrollChangedListener;

    /**
     * 设置webview点击监听
     */
    private OnSrollWebViewOnClickListener mOnSrollWebViewListener;

    private Scroller mScroller;

    public OnScrollToTopListener getScrollChanged()
    {
        return mScrollChangedListener;
    }


    /**
     * 主要是用在用户手指离开WebView，WebView还在继续滑动，我们用来保存Y的距离，然后做比较
     */
    private int mLastScrollY;
    private int mDelayedCount;
    private static final int MAX_DELAYED_COUNT = 10;
    /**
     * 用于用户手指离开WebView的时候获取WebView滚动的Y距离
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg)
        {
            int scrollY = getScrollY();

            // 此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
            if (mScrollChangedListener != null)
                mScrollChangedListener.setOnScrollTop(scrollY);
            if (mLastScrollY != scrollY)
            {
                mDelayedCount = 0;
                mLastScrollY = scrollY;
                handler.sendMessageDelayed(handler.obtainMessage(), 5);
            }
            else
            {
                // 多次循环检测，防止bug
                if (mDelayedCount != MAX_DELAYED_COUNT)
                {
                    mDelayedCount++;
                    handler.sendMessageDelayed(handler.obtainMessage(), 5);
                }
                else
                {
                    mDelayedCount = 0;
                    if (mScrollChangedListener != null)
                    {
                        mScrollChangedListener.setOnScrollFling(false);
                    }
                }
            }

        }

    };

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        if (mScrollChangedListener != null)
        {
            mScrollChangedListener.setOnScrollFling(true);
        }
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_UP:
                handler.sendMessageDelayed(handler.obtainMessage(), 1);
                if (mOnSrollWebViewListener != null)
                {
                    mOnSrollWebViewListener.scrollWebViewOnClick();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void setStickScrollListener(OnScrollToTopListener scrollChangedListener)
    {
        mScrollChangedListener = scrollChangedListener;
    }


    public void setScrollWebViewClickListener(OnSrollWebViewOnClickListener onSrollWebViewListener)
    {
        this.mOnSrollWebViewListener = onSrollWebViewListener;
    }
    /**
     * 调用此方法滚动到目标位置
     *
     * @param fx x
     * @param fy y
     */
    @Override
    public void smoothScrollTo(int fx, int fy)
    {
        int dx = fx - getScrollX();
        int dy = fy - getScrollY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 调用此方法设置滚动的相对偏移
     *
     * @param dx x
     * @param dy y
     */
    @Override
    public void smoothScrollBy(int dx, int dy)
    {
        // 设置mScroller的滚动偏移量
        mScroller.startScroll(getScrollX(), getScrollY(), dx, dy, 500);
        invalidate();// 这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll()
    {
        // 先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset())
        {
            // 这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }

    @Override
    public void destroy()
    {
        super.destroy();
        mScroller = null;
        mScrollChangedListener = null;
        mScrollChangedListener = null;
    }
}
