package cn.com.kunghsu.desktop.system.worker;

import cn.com.kunghsu.desktop.system.LocalComputer;
import cn.com.kunghsu.desktop.util.Log;

/**
 */
public class CaptureWorker extends BaseWorker
{
    private void captureAndStore() throws Exception
    {
        ScreenImages.addScreenshot(LocalComputer.captureScreen());
    }

    public void run()
    {
        while (!this.isTerminated())
        {
            try
            {
                captureAndStore();
                // TODO: FPS控制
                sleep(50);
            }
            catch(Exception e)
            {
                Log.error(e);
            }
        }
    }
}
