package me.ycdev.android.devtools.sampler;

import java.io.FileWriter;
import java.util.ArrayList;

class SampleTaskInfo {
    public ArrayList<String> pkgNames;
    public int sampleInterval; // in seconds
    public int samplePeriod; // in minutes
    public long startTime;
    public long sampleClockTime;
    public int sampleCount;

    public boolean isSampling;
    public ArrayList<FileWriter> fileWriters;
    public AppsSetStat preAppsSetStat;

}
