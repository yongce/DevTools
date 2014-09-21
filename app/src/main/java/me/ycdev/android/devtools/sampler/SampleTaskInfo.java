package me.ycdev.android.devtools.sampler;

import java.util.ArrayList;

class SampleTaskInfo {
    public ArrayList<String> pkgNames;
    public int sampleInterval; // in seconds
    public long startTime;
    public long sampleClockTime;
    public int sampleCount;
    public boolean isSampling;
}
