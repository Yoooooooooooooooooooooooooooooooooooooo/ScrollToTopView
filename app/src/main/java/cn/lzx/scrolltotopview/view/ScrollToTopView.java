package cn.lzx.scrolltotopview.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import cn.lzx.scrolltotopview.R;

/**
 * Project Name: ScrollToTopView
 * File Name:    ScrollToTopView.java
 * ClassName:    ScrollToTopView
 *
 * Description: 滚动到顶部控件
 *
 * @author LZX
 * @date 2016年09月09日 10:45
 *
 */

public class ScrollToTopView extends FrameLayout implements View.OnClickListener,
                                                            AbsListView.OnScrollListener,
                                                            Animation.AnimationListener
{

    private View mView;
    private AbsListView.OnScrollListener mOnScrollListener;
    private ImageButton mButton;
    private final static int STATE_TOP = 1;// 列表在顶部
    private final static int STATE_SCROLL = 2;// 列表不在顶部
    private int mShowState;// 新状态
    private int mLastShowState;// 旧状态
    private Animation mAnimationOutToRight;// 出现动画
    private Animation mAnimationInFromRight;// 消失动画
    private final static int ANIMATION_DURATION = 500;// 动画时间
    private boolean mIsShowAnim;
    private Animation mAnimationFadeIn;
    private Animation mAnimationFadeOut;
    private boolean mIsScrolling;
    private OnStickButtonClickListener mGoToTopButtonClickListener;
    private int mListViewScrollState;
    private IStickButtonViewStateChangeListener mStickButtonListener;
    private boolean mShowEnable = true;

    public ScrollToTopView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public ScrollToTopView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public ScrollToTopView(Context context)
    {
        super(context);
        init(context);
    }


    private void init(Context context)
    {
        mButton = new ImageButton(getContext());
        mButton.setOnClickListener(this);
        try
        {
            mButton.setBackgroundResource(R.drawable.selector_go_top_btn_bg);
        }
        catch (Resources.NotFoundException e)
        {
            e.printStackTrace();
        }

        mButton.setPadding(0, 0, 4, 0);
        addView(mButton);
        mButton.setVisibility(GONE);
        mShowState = STATE_TOP;
        mLastShowState = mShowState;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        int margin = 10;
        int mWidth = getMeasuredWidth();
        int mHeight = getMeasuredHeight();
        mButton.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int buttonWidth = mButton.getMeasuredWidth();
        int buttonHeight = mButton.getMeasuredHeight();
        mButton.layout(mWidth - margin - buttonWidth,
                       mHeight - margin - buttonHeight,
                       mWidth - margin,
                       mHeight - margin);
        if (mButton.isShown())
            mButton.bringToFront();
    }

    /**
     * 设置是否可以显示顶置按钮
     * @param enable
     */
    public void setShowEnable(boolean enable)
    {
        this.mShowEnable = enable;
    }

    private OnScrollToTopListener mStickScrollListener = new OnScrollToTopListener() {

        @Override
        public void setOnScrollTop(int y)
        {
            if (y <= 0)
            {
                mShowState = STATE_TOP;
            }
            else
            {
                mShowState = STATE_SCROLL;
            }
            setButtonShowAnimation();
        }

        @Override
        public void setOnScrollFling(boolean isScroll)
        {
            //setButtonFadeAnimation(isScroll);
            setFadeAnimation(isScroll);
        }
    };

    /**
     * 用原有监听器防止破坏原有自定义listview的功能
     *
     * @param listView
     * @param onScrollListener
     */
    public void setListView(ListView listView, AbsListView.OnScrollListener onScrollListener)
    {
        mView = listView;
        mOnScrollListener = onScrollListener;
        listView.setOnScrollListener(this);
    }



    public void setWebView(ScrollWebView webView)
    {
        mView = webView;
        if (mView != null)
        {
            ((SmoothScrollable) mView)
                    .setStickScrollListener(mStickScrollListener);
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v)
    {
        if (mView != null && v == mButton)
        {
            if (mView instanceof ListView)
            {
                //		if (((ListView) mView).getFirstVisiblePosition() > 8)
                //		    ((ListView) mView).setSelection(8);
                //		((ListView) mView).smoothScrollToPosition(0);

                ListView listView= (ListView) mView;
                listView.setSelection(0);

                if(mGoToTopButtonClickListener!=null)
                {
                    mGoToTopButtonClickListener.onGoToViewTop();
                }
            }
            else if (mView instanceof SmoothScrollable)
            {
                ((SmoothScrollable) mView).smoothScrollTo(0, 0);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        mListViewScrollState = scrollState;
        setButtonFadeAnimation(scrollState != SCROLL_STATE_IDLE);
        if (mOnScrollListener != null)
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        if (mViewScrollStateChangeListener != null)
            mViewScrollStateChangeListener.onScrollStateChange(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount)
    {
        if (firstVisibleItem <= 1)
        {
            mShowState = STATE_TOP;
        }
        else
        {
            mShowState = STATE_SCROLL;
        }
        if (mListViewScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
        {
            setButtonShowAnimation();
            //滑动过程中，都是true
            callBackStickButtonChange(true);
        }
        if (mOnScrollListener != null)
            mOnScrollListener.onScroll(view,
                                       firstVisibleItem,
                                       visibleItemCount,
                                       totalItemCount);
    }

    /**
     * 给WebView设置滑动监听
     */
    public interface OnScrollChangedListener
    {
        public void setOnScrollTop(int y);

        public void setOnScrollFling(boolean isScroll);
    }

    /**
     * 回到顶部监听
     */
    public interface OnStickButtonClickListener
    {
        void onGoToViewTop();
    }

    public void setOnGoToTopButtonClickListener(OnStickButtonClickListener goToTopButtonClickListener)
    {
        this.mGoToTopButtonClickListener=goToTopButtonClickListener;
    }

    /**
     * 设置按钮出现动画
     */
    private void setButtonShowAnimation()
    {
        if (!mShowEnable)
        {
            return;
        }
        if (mLastShowState != mShowState)
        {
            mButton.clearAnimation();
            switch (mShowState)
            {
                case STATE_TOP:
                    mAnimationOutToRight = AnimationUtils
                            .loadAnimation(getContext(),
                                           R.anim.out_to_right_half_alpha);
                    mAnimationOutToRight.setFillAfter(true);
                    mAnimationOutToRight.setDuration(ANIMATION_DURATION);
                    mButton.startAnimation(mAnimationOutToRight);
                    mAnimationOutToRight.setAnimationListener(this);
                    break;

                case STATE_SCROLL:
                    mAnimationInFromRight = AnimationUtils
                            .loadAnimation(getContext(),
                                           R.anim.in_from_right_half_alpha);
                    mAnimationInFromRight.setFillAfter(true);
                    mAnimationInFromRight.setDuration(ANIMATION_DURATION);
                    mButton.setAnimation(mAnimationInFromRight);
                    mAnimationInFromRight.setAnimationListener(this);
                    break;
            }
            mLastShowState = mShowState;
        }
        removeCountDown();
    }

    /**
     * 按钮淡入淡出动画
     */
    private void setButtonFadeAnimation(boolean isScroll)
    {
        if (!mShowEnable)
        {
            return;
        }
        if (!mIsShowAnim && mButton.isShown() && mIsScrolling != isScroll && mShowState != STATE_TOP)
        {
            mButton.clearAnimation();
            fadeAnimation(isScroll);
        }
        mIsScrolling = isScroll;
    }

    private void setFadeAnimation(boolean isScroll)
    {
        if (!mIsShowAnim && mButton.isShown() && mIsScrolling != isScroll)
        {
            mButton.clearAnimation();
            fadeAnimation(isScroll);
        }
        mIsScrolling = isScroll;
    }



    private void fadeAnimation(boolean isScroll)
    {
        if (isScroll)
        {
            mAnimationFadeOut = AnimationUtils.loadAnimation(getContext(),
                                                             R.anim.out_to_half_alpha);
            mAnimationFadeOut.setFillAfter(true);
            mAnimationFadeOut.setDuration(ANIMATION_DURATION);
            mButton.setAnimation(mAnimationFadeOut);
        }
        else
        {
            mAnimationFadeIn = AnimationUtils.loadAnimation(getContext(),
                                                            R.anim.in_from_half_alpha);
            mAnimationFadeIn.setFillAfter(true);
            mAnimationFadeIn.setDuration(ANIMATION_DURATION);
            mButton.setAnimation(mAnimationFadeIn);
            mAnimationFadeIn.setAnimationListener(this);
        }
    }

    @Override
    public void onAnimationStart(Animation animation)
    {
        mButton.setVisibility(VISIBLE);
        if (animation == mAnimationInFromRight || animation == mAnimationOutToRight)
        {
            mIsShowAnim = true;
        }
    }

    @Override
    public void onAnimationEnd(Animation animation)
    {
        if (animation == mAnimationInFromRight)
        {
            mButton.setVisibility(VISIBLE);
            mButton.setClickable(true);
            mIsShowAnim = false;
            if (!mIsScrolling)
                fadeAnimation(false);
        }
        else if (animation == mAnimationOutToRight)
        {
            mButton.setVisibility(GONE);
            mButton.setClickable(false);
            mIsShowAnim = false;
            callBackStickButtonChange(mIsShowAnim);
        }
        else if (animation == mAnimationFadeIn)
        {
            mCountTime = COUNT_TIME;
            startCountDown();
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation)
    {
    }

    final Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run()
        {
            if (mCountTime == -1)
            {
                return;
            }
            else
            {
                mHandler.postDelayed(this, 1000);
                mCountTime--;
            }
            if (mCountTime == 0)
            {
                mShowState = STATE_TOP;
                setButtonShowAnimation();
            }
        }
    };

    private final static int COUNT_TIME = 2;
    private int mCountTime = COUNT_TIME;

    /**
     * 移除计时器
     */
    private void removeCountDown()
    {
        mCountTime = -1;
        mHandler.removeCallbacks(mRunnable);
    }

    /**
     * 开始倒计时
     */
    private void startCountDown()
    {
        mHandler.postDelayed(mRunnable, 1000);
    }


    private IViewScrollStateChangeListener mViewScrollStateChangeListener;

    public interface IViewScrollStateChangeListener
    {
        void onScrollStateChange(View view, int scrollState);
    }

    public void setScrollStateChangeListener(IViewScrollStateChangeListener viewScrollStateChangeListener)
    {
        this.mViewScrollStateChangeListener=viewScrollStateChangeListener;
    }


    /**
     * 设置Button的显示和隐藏的回调
     * @param listener
     */
    public void setStickButtonViewStateChangeListener(IStickButtonViewStateChangeListener listener)
    {
        this.mStickButtonListener =listener;
    }

    public interface IStickButtonViewStateChangeListener
    {
        void onStickButtonChange(boolean isVisible);
    }

    private void callBackStickButtonChange(boolean buttonIsVisible)
    {
        if(mStickButtonListener !=null)
        {
            mStickButtonListener.onStickButtonChange(buttonIsVisible);
        }
    }

}

