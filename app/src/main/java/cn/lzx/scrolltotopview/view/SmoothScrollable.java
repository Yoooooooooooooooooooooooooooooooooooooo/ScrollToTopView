package cn.lzx.scrolltotopview.view;

/**
 * Project Name: ScrollToTopView
 * File Name:    SmoothScrollable.java
 * ClassName:    SmoothScrollable
 *
 * Description: 可平滑滚动接口.
 *
 * @author LZX
 * @date 2016年09月09日 10:56
 *
 */

public interface SmoothScrollable
{
    /**
     * 滚动到指定位置
     * @param fx
     * @param fy
     */
    void smoothScrollTo(int fx, int fy);

    /**
     * 滚动到相对位置
     * @param dx
     * @param dy
     */
    void smoothScrollBy(int dx, int dy);

    /**
     * 设置指定滚动变化监听器
     * @param scrollChangedListener
     */
    void setStickScrollListener(OnScrollToTopListener scrollChangedListener);
}
