package pass;

import driver.Config;
import ir.values.Module;
import pass.analyze.BuildCFG;
import pass.analyze.DomInfo;
import pass.analyze.LoopInfoAnalysis;
import pass.analyze.SideEffectAnalysis;
import pass.refactor.*;

import java.util.ArrayList;


/**
 * 似乎 GVN 和 GCM 只能进行 1 次，当进行超过一次后，就会出现一些奇怪的 bug
 * 这些 bug 可能是某些底层数据结构导致的，由于时间原因，我没有办法修改了
 * 在这个版本下，似乎还是有错误的，建议在考试的时候不开启 inlineFunction，可以达到很好的效果
 * 如果在课上接着卡这个点，可以考虑先 gvn, gcm, 然后函数内联，可以有很好的效果
 * 但是其实先开始 inlineFunction，然后 gvn，gcm，依然是可以全部 AK 的，可以考虑这样操作
 * 看来只要不涉及 DCE，就不会 bug，但是具体是怎样的，我也不清楚
 */
public class PassManager
{
    private final Module module = Module.getInstance();
    private final ArrayList<Pass> passes = new ArrayList<>();

    public void run()
    {
        if (Config.isO1)
        {
            passes.add(new BuildCFG());
            passes.add(new DomInfo());
            passes.add(new LoopInfoAnalysis());
            passes.add(new GlobalVariableLocalize());
            passes.add(new Mem2reg());
            // 因为 mem2reg 会减少内存访存，所以此时才进行副作用分析
            passes.add(new SideEffectAnalysis());
            passes.add(new UselessRetEmit());
//        passes.add(new GVN());
            passes.add(new DeadCodeEmit());
//        passes.add(new GCM(true));
            passes.add(new BranchOpt());
            // 完成函数内联后，需要重新进行 DomInfo，LoopInfo，SideEffect 的分析
            passes.add(new InlineFunction());
            passes.add(new BuildCFG());
            passes.add(new DomInfo());
            passes.add(new LoopInfoAnalysis());
            passes.add(new SideEffectAnalysis());
            // 这些 pass 都不会改变分支结构
            passes.add(new GVN());
            passes.add(new DeadCodeEmit());
            passes.add(new GCM());
            passes.add(new BranchOpt());
        }
        else
        {
            // 这个顺序是全部 ak 的，但是我依然有些担心
            // 可以考虑将 GVL，Inline 去掉，应该就是 100% 了
            passes.add(new BuildCFG());
            passes.add(new DomInfo());
            passes.add(new LoopInfoAnalysis());
            passes.add(new GlobalVariableLocalize());
            passes.add(new Mem2reg());
            passes.add(new SideEffectAnalysis());
            passes.add(new InlineFunction());
            passes.add(new BuildCFG());
            passes.add(new DomInfo());
            passes.add(new LoopInfoAnalysis());
            passes.add(new SideEffectAnalysis());
            passes.add(new GVN());
            passes.add(new GCM());
            passes.add(new BranchOpt());
        }


        for (Pass pass : passes)
        {
            pass.run();
        }
    }
}
