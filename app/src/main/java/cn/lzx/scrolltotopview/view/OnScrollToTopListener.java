package cn.lzx.scrolltotopview.view;

/**
 * Project Name: ScrollToTopView
 * File Name:    OnScrollToTopListener.java
 * ClassName:    OnScrollToTopListener
 *
 * Description: 滚动到顶部监听
 *
 * @author LZX
 * @date 2016年09月09日 10:49
 *
 */

public interface OnScrollToTopListener
{
    public void setOnScrollTop(int y);

    public void setOnScrollFling(boolean isScroll);
}
