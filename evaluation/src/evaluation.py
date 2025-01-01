import json
import subprocess
import os
import shutil
import time
import sys

import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np
import statsmodels.api as sm


PATH_TO_CMENDER_JAR = "~/Desktop/tese/code-mending/cmender/build/libs/cmender.jar"
PATH_TO_DIAG_EXPORTER = "~/Desktop/tese/code-mending/diag-exporter/cmake-build-debug/diag-exporter"
VM_OPTIONS = "-Dlog.level=debug -Dlog.dir=../.logs -Ddata.dir=../.data"

def histogram_util(df, column,
                   bins=40, bincolor="skyblue", binedgecolor="blue",
                   linecolor="red", linestyle="dashed", linewidth=1,
                   title=None, xlabel=None, ylabel="Frequency",
                   save_path=None, figname=None):
    sns.histplot(df[column], bins=bins, color="skyblue", edgecolor="blue")

    plt.hist(df[column], bins=bins, color=bincolor, edgecolor=binedgecolor)
    plt.axvline(df[column].mean(), color=linecolor, linestyle=linestyle, linewidth=linewidth)

    if title:
        plt.title(title)

    if xlabel:
        plt.xlabel(xlabel)

    if ylabel:
        plt.ylabel(ylabel)

    plt.legend(["Mean"])

    if save_path:
        plt.savefig(os.path.join(save_path, figname))
        plt.close()
    else:
        plt.show()

def scatter_plot_util(df, x_column, y_column,
                      s=5.0, color="skyblue", edgecolors="blue", linewidths=0.8, adjust_left=None,
                      title=None, xlabel=None, ylabel=None,
                      save_path=None, figname=None):
    sns.scatterplot(data=df, x=x_column, y=y_column, s=s, color=color, edgecolors=edgecolors,
                linewidths=linewidths)
    #sns.regplot(x=x_column, y=y_column, data=df, scatter=False, color='red', line_kws={"linewidth":2})

    if title:
        plt.title(title)

    if xlabel:
        plt.xlabel(xlabel)

    if ylabel:
        plt.ylabel(ylabel)

    if adjust_left:
        plt.subplots_adjust(left=adjust_left)

    if save_path:
        plt.savefig(os.path.join(save_path, figname))
        plt.close()
    else:
        plt.show()

def line_plot_util(df, x_column, y_column, adjust_left=None,
                     title=None, xlabel=None, ylabel=None,
                     save_path=None, figname=None):
     sns.lineplot(data=df, x=x_column, y=y_column, color="skyblue", legend=False)
    
     if title:
          plt.title(title)
    
     if xlabel:
          plt.xlabel(xlabel)
    
     if ylabel:
          plt.ylabel(ylabel)
    
     if adjust_left:
          plt.subplots_adjust(left=adjust_left)
    
     if save_path:
          plt.savefig(os.path.join(save_path, figname))
          plt.close()
     else:
          plt.show()

def multi_line_plot_util(df, x_column, y_column, hue_column, 
                        s=5.0, color="skyblue", edgecolors="blue", linewidths=0.8, adjust_left=None,
                        title=None, xlabel=None, ylabel=None,
                        save_path=None, figname=None):
    sns.lineplot(data=df, x=x_column, y=y_column, hue=hue_column, palette="husl", legend=False)

    if title:
        plt.title(title)
    
    if xlabel:
        plt.xlabel(xlabel)
    
    if ylabel:
        plt.ylabel(ylabel)
    
    if save_path:
        plt.savefig(os.path.join(save_path, figname))
        plt.close()
    else:
        plt.show()

def multi_scatter_plot_util(df, x_column, y_column, hue_column, 
                        s=5.0, color="skyblue", edgecolors="blue", linewidths=0.8, adjust_left=None,
                        title=None, xlabel=None, ylabel=None,
                        save_path=None, figname=None):
    sns.scatterplot(data=df, x=x_column, y=y_column, hue=hue_column, palette="coolwarm", legend=False)
#s=s, edgecolors=edgecolors, linewidths=linewidths
    if title:
        plt.title(title)
    
    if xlabel:
        plt.xlabel(xlabel)
    
    if ylabel:
        plt.ylabel(ylabel)
    
    if save_path:
        plt.savefig(os.path.join(save_path, figname))
        plt.close()
    else:
        plt.show()

class Evaluation:    
    def run_cmender_on_files(self, files, output_dirpath):
        #print("Running cmender on all files")

        all_file_paths = " ".join(files)

        # save in file for debugging
        with open("all_file_paths.txt", "w") as f:
            f.write(all_file_paths)

        #print("All file paths: ", all_file_paths)

        command = f"java {VM_OPTIONS} -jar {PATH_TO_CMENDER_JAR} -dex {PATH_TO_DIAG_EXPORTER} -mendfile-cpi -diags-cpi -od {all_file_paths} -o {output_dirpath} -t 12 -rps -a BasicMultiplePPErrorsAnalysis"

        result = subprocess.run(command, shell=True, capture_output=True, text=True)

    def read_cmender_report(self, cmender_output_dir):
        try:
            with open(f"{cmender_output_dir}/cmender_report.json") as result_file:
                return json.load(result_file)
        except Exception as e:
            print("Exception while reading cmender_report: ", e)
            return None
    
    def read_cmender_source_report(self, source_report_path):
        try:
            with open(f"{source_report_path}/source_report.json") as result_file:
                source_report = json.load(result_file)
            return source_report
        except Exception as e:
            print("Exception while reading source_report: ", e)
            return None

    def min_max_normalize_file_progress(self, cmender_output_dir):
        normalized_file_progresses = []

        for source_report_dirname in os.listdir(cmender_output_dir):
            source_report_path = os.path.join(cmender_output_dir, source_report_dirname)

            if not os.path.isdir(source_report_path):
                continue

            source_report = self.read_cmender_source_report(source_report_path)

            if source_report is None:
                print("Could not read source report for item: ", source_report_dirname)
                continue
        
            # Get the completion status range for normalization
            min_file_progress = float('inf')
            max_file_progress = float('-inf')
            
            # Find min and max file_progress for this source result
            for iteration in source_report["mendingIterations"]:
                min_file_progress = min(min_file_progress, iteration["terminationStatus"]["fileProgress"])
                max_file_progress = max(max_file_progress, iteration["terminationStatus"]["fileProgress"])

            # Avoid division by zero if all file_progress values are identical
            if min_file_progress == max_file_progress:
                # If all values are the same, we can just assign the same value to normalized_file_progress
                for iteration in source_report["mendingIterations"]:
                    normalized_file_progress = 1.0 if min_file_progress > 0 else 0.0  # or just use min_file_progress or max_file_progress
                    normalized_file_progresses.append(normalized_file_progress)

            else:
                # Normalize file progresses for the current source result
                for iteration in source_report["mendingIterations"]:
                    
                    # Get the original file progress
                    file_progress = iteration["terminationStatus"]["fileProgress"]
                    
                    # Normalize it within the [0, 1] range of that specific source result
                    normalized_file_progress = (file_progress - min_file_progress) / (max_file_progress - min_file_progress)
                    
                    # Append normalized values
                    normalized_file_progresses.append(normalized_file_progress)
            
        return normalized_file_progresses

    def analyze_cmender_results(self, cmender_report, cmender_output_dir, project_name):
        source_results_data = [None] * cmender_report["totalFiles"]

        i = 0

        for source_report_dirname in os.listdir(cmender_output_dir):
            source_report_path = os.path.join(cmender_output_dir, source_report_dirname)

            if not os.path.isdir(source_report_path):
                continue

            source_report = self.read_cmender_source_report(source_report_path)

            if source_report is None:
                print("Could not read source report for item: ", source_report_dirname)
                continue

            fatal_exception = source_report["fatalException"]

            if fatal_exception is not None:
                print("Fatal exception: ", fatal_exception["message"], " for file: ", source_report["sourceFile"][80:])
                #pass

            #if fatal_exception is not None and "could not create mending dir" in fatal_exception["message"]:
            #    print("Fatal exception: ", fatal_exception, " for file: ", source_report["sourceFile"])

            source_results_data[i] = (
                project_name, # project
                source_report["sourceFile"], # file_path
                source_report["fileSize"]["bytes"], # file_size_b
                source_report["fileSize"]["kilobytes"], # file_size_kb
                source_report["success"], # success
                source_report["fatalException"] is not None, # fatal_exception
                source_report["iterationCount"], # iterations
                source_report["completionStatusEstimate"] * source_report["fileSize"]["kilobytes"], # last_file_offset
                source_report["completionStatusEstimate"], # file_progress
                source_report["completionStatusEstimate"] * source_report["fileSize"]["kilobytes"] - source_report["fileSize"]["kilobytes"], # last_file_offset_residual
                source_report["totalTime"]["millis"], # total_time_ms
                source_report["diagExporterTotalTime"]["ratio"], # diag_exporter_total_time_ratio
                source_report["mendfileSize"]["bytes"], # mendfile_size_b
                source_report["mendfileSize"]["kilobytes"], # mendfile_size_kb
                source_report["totalTime"]["millis"] / source_report["fileSize"]["kilobytes"], # file_size_kb_normalized_total_time_ms
                source_report["iterationCount"] / source_report["fileSize"]["kilobytes"], # file_size_kb_normalized_iterations
                0, # spearman_corr_file_progress_iterations
                0, # spearman_corr_norm_min_max_file_progress_total_time
                0, # spearman_corr_norm_percentile_file_progress_total_time
                0, # spearman_corr_norm_global_file_progress_iterations
                0, # spearman_corr_norm_weight_file_progress_total_time
            )

            i += 1

        if i != cmender_report["totalFiles"]:
            print("Mismatch between totalFiles and files processed")

        source_results_df = pd.DataFrame(source_results_data, columns=[
            "project",
            "file_path",
            "file_size_b",
            "file_size_kb",
            "success",
            "fatal_exception",
            "iterations",
            "last_file_offset",
            "file_progress",
            "last_file_offset_residual",
            "total_time_ms",
            "diag_exporter_total_time_ratio",
            "mendfile_size_b",
            "mendfile_size_kb",
            "file_size_kb_normalized_total_time_ms",
            "file_size_kb_normalized_iterations",
            "spearman_corr_file_progress_iterations",
            "spearman_corr_norm_min_max_file_progress_total_time",
            "spearman_corr_norm_percentile_file_progress_total_time",
            "spearman_corr_norm_global_file_progress_iterations",
            "spearman_corr_norm_weight_file_progress_total_time",
        ])

        iteration_results_data = []

        min_max_normalized_file_progresses = self.min_max_normalize_file_progress(cmender_output_dir)

        i = 0

        for source_report_dirname in os.listdir(cmender_output_dir):
            source_report_path = os.path.join(cmender_output_dir, source_report_dirname)
            
            if not os.path.isdir(source_report_path):
                continue

            source_report = self.read_cmender_source_report(source_report_path)

            if source_report is None:
                print("Could not read source report for item: ", source_report_dirname)
                continue

            iteration_count = source_report["iterationCount"]
            #print("Iteration count: ", iteration_count, " for file: ", source_result["sourceFile"])
            for j, mending_iteration in enumerate(source_report["mendingIterations"]):
                iteration_results_data.append((
                    source_report["sourceFile"], # file_path
                    j+1, # iteration number
                    (j+1) * 100 / iteration_count, # iteration_percentage (normalized)
                    mending_iteration["terminationStatus"]["fileProgress"], # file progress (0-1)
                    min_max_normalized_file_progresses[j], # min-max normalized file progress (0-1)
                    0, # percentile normalized file progress (0-1)
                    0, # global scaling normalized file progress (0-1)
                    0, # hybrid normalized file progress (0-1)
                ))
            
            i += 1

        iteration_results_df = pd.DataFrame(iteration_results_data, columns=[
            "file_path",
            "iteration_number",
            "iteration_percentage",
            "file_progress",
            "min_max_normalized_file_progress",
            "percentile_normalized_file_progress",
            "global_scaling_normalized_file_progress",
            "weighted_normalized_file_progress",
        ])
        
        # 1. Percentile-based Normalization
        iteration_results_df["percentile_normalized_file_progress"] = \
            iteration_results_df.groupby("file_path")["file_progress"] \
                .transform(lambda x: x.rank(method="max") / len(x))

        # 2. Global Scaling
        global_min = iteration_results_df["file_progress"].min()
        global_max = iteration_results_df["file_progress"].max()
        iteration_results_df["global_scaling_normalized_file_progress"] = (iteration_results_df["file_progress"] - global_min) / (global_max - global_min)

        # 3. Weighted Normalization (Hybrid Approach)
        iteration_results_df["weighted_normalized_file_progress"] = iteration_results_df.groupby("file_path")["file_progress"].transform(
            lambda x: (x - x.min()) / (global_max - global_min)
        )

        #print("spearman file ", iteration_results_df[["file_progress", "iteration_percentage"]].corr(method="spearman").iloc[0, 1])
        #print("spearman norm min max", iteration_results_df[["min_max_normalized_file_progress", "iteration_percentage"]].corr(method="spearman").iloc[0, 1])
        #print("spearman percent norm", iteration_results_df[["percentile_normalized_file_progress", "iteration_percentage"]].corr(method="spearman").iloc[0, 1])
        #print("spearman global scale norm", iteration_results_df[["global_scaling_normalized_file_progress", "iteration_percentage"]].corr(method="spearman").iloc[0, 1])
        #print("spearman weight norm", iteration_results_df[["weighted_normalized_file_progress", "iteration_percentage"]].corr(method="spearman").iloc[0, 1])
    
        return source_results_df, iteration_results_df
    
    def get_aggr_source_results(self, source_results_df, project_name):
        total = len(source_results_df)
        success_count = source_results_df["success"].sum()
        unsuccessful_count = len(source_results_df) - success_count
        fatal_exception_count = source_results_df["fatal_exception"].sum()

        success_ratio = success_count / len(source_results_df)
        unsuccessful_ratio = 1 - success_ratio
        fatal_exception_ratio = fatal_exception_count / len(source_results_df)
        fatal_exception_over_unsuccessful_ratio = 0 if unsuccessful_count == 0 else fatal_exception_count / unsuccessful_count

        total_time_ms_mean = source_results_df["total_time_ms"].mean()
        diag_exporter_total_time_ratio_mean = source_results_df["diag_exporter_total_time_ratio"].mean()
        iterations_mean = source_results_df["iterations"].mean()
        file_size_kb_mean = source_results_df["file_size_kb"].mean()
        mendfile_size_kb_mean = source_results_df["mendfile_size_kb"].mean()
        file_progress_mean = source_results_df["file_progress"].mean()

        total_time_ms_var = source_results_df["total_time_ms"].var()
        diag_exporter_total_time_ratio_var = source_results_df["diag_exporter_total_time_ratio"].var()
        iterations_var = source_results_df["iterations"].var()
        file_size_kb_var = source_results_df["file_size_kb"].var()
        mendfile_size_kb_var = source_results_df["mendfile_size_kb"].var()
        file_progress_var = source_results_df["file_progress"].var()

        X = source_results_df["file_size_kb"]  # Independent variable (file size)
        X = sm.add_constant(X)  # Adds a constant (intercept) to the model
        y = source_results_df["success"]  # Dependent variable (success)
        
        #print(X.dtypes)  # Check the data types of all independent variables
        #print(y.dtypes)  # Check the data type of the dependent variable

        model = sm.Logit(y, X)
        result = model.fit()

        print(result.summary())
        print("\n\n")

        file_size_kb_coef = result.params.iloc[1]
        file_size_kb_p_value = result.pvalues.iloc[1]
        const = result.params.iloc[0]
        const_p_value = result.pvalues.iloc[0]
        prsquared = result.prsquared
        

        X = source_results_df["file_size_kb"]  # Independent variable (file size)
        X = sm.add_constant(X)
        y = source_results_df["last_file_offset_residual"] # Dependent variable (last file offset)

        # Fit the OLS regression model
        model = sm.OLS(y, X).fit()

        # Print the regression summary
        print(model.summary())
        print("\n\n")

        file_size_kb_coef = model.params.iloc[1]
        file_size_kb_p_value = model.pvalues.iloc[1]
        const = model.params.iloc[0]
        const_p_value = model.pvalues.iloc[0]
        rsquared = model.rsquared

        print("File size coef: ", file_size_kb_coef)
        print("File size p-value: ", file_size_kb_p_value)
        print("Const coef: ", const)
        print("Const p-value: ", const_p_value)
        print("R-squared: ", rsquared)

        return (
                project_name, total, success_count, unsuccessful_count, fatal_exception_count,
                success_ratio, unsuccessful_ratio, fatal_exception_ratio, fatal_exception_over_unsuccessful_ratio,
                total_time_ms_mean, diag_exporter_total_time_ratio_mean, iterations_mean, file_size_kb_mean, mendfile_size_kb_mean, file_progress_mean,
                total_time_ms_var, diag_exporter_total_time_ratio_var, iterations_var, file_size_kb_var, mendfile_size_kb_var, file_progress_var,
                const, const_p_value, file_size_kb_coef, file_size_kb_p_value, prsquared, rsquared
            )

    # Histograms
    def file_size_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "file_size_kb", bins=10, title="File Size Histogram", xlabel="File Size (KiB)",
                    save_path=histograms_dir, figname="file_size_hist.pdf")

    def file_progress_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "file_progress", bins=10, title="File Progress Histogram", xlabel="File Progress",
                    save_path=histograms_dir, figname="file_progress_hist.pdf")

    def iteration_count_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "iterations", bins=20, title="Iteration Count Histogram", xlabel="Iterations",
                    save_path=histograms_dir, figname="iteration_count_hist.pdf")

    def total_time_ms_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "total_time_ms", bins=20, title="Total Time Histogram", xlabel="Total Time (ms)",
                    save_path=histograms_dir, figname="total_time_ms_hist.pdf")

    def diag_exporter_total_time_ratio_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "diag_exporter_total_time_ratio", bins=20, title="diag-exporter Total Time Ratio Histogram", xlabel="diag-exporter Total Time Ratio",
                    save_path=histograms_dir, figname="diag_exporter_total_time_ratio_hist.pdf")

    def mendfile_size_histogram(self, source_results_df, histograms_dir):
        histogram_util(source_results_df, "mendfile_size_kb", bins=20, title="Mendfile Size Histogram", xlabel="Mendfile Size (KiB)",
                    save_path=histograms_dir, figname="mendfile_size_hist.pdf")

    # Line plots
    def file_progress_vs_iterations_percentage_multi_line_plot(self, iteration_results_df, line_plots_dir):
        if iteration_results_df is None:
            return
        multi_line_plot_util(iteration_results_df, "iteration_percentage", "file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=line_plots_dir, figname="file_progress_vs_iterations_percentage_multi_line_plot.pdf")

    def normalized_file_progress_vs_iterations_percentage_multi_line_plot(self, iteration_results_df, line_plots_dir):
        if iteration_results_df is None:
            return

        multi_line_plot_util(iteration_results_df, "iteration_percentage", "min_max_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=line_plots_dir, figname="min_max_normalized_file_progress_vs_iterations_percentage_multi_line_plot.pdf")

        multi_line_plot_util(iteration_results_df, "iteration_percentage", "percentile_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=line_plots_dir, figname="percentile_normalized_file_progress_vs_iterations_percentage_multi_line_plot.pdf")

        multi_line_plot_util(iteration_results_df, "iteration_percentage", "global_scaling_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=line_plots_dir, figname="global_scaling_normalized_file_progress_vs_iterations_percentage_multi_line_plot.pdf")
        
        multi_line_plot_util(iteration_results_df, "iteration_percentage", "weighted_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=line_plots_dir, figname="weighted_normalized_file_progress_vs_iterations_percentage_multi_line_plot.pdf")

    # Scatter plots
    def file_progress_vs_iteration_count_scatter_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "iterations", "file_progress", adjust_left=0.2, 
                          title="File Progress vs Iterations", xlabel="Iterations", ylabel="File Progress",
                            save_path=scatter_plots_dir, figname="file_progress_vs_iterations_scatter_plot.pdf")

    def file_progress_vs_iterations_percentage_multi_scatter_plot(self, iteration_results_df, scatter_plots_dir):
        if iteration_results_df is None:
            return
        multi_scatter_plot_util(iteration_results_df, "iteration_percentage", "file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=scatter_plots_dir, figname="file_progress_vs_iterations_percentage_multi_scatter_plot.pdf")

    def normalized_file_progress_vs_iterations_percentage_multi_scatter_plot(self, iteration_results_df, scatter_plots_dir):
        if iteration_results_df is None:
            return
        multi_scatter_plot_util(iteration_results_df, "iteration_percentage", "min_max_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=scatter_plots_dir, figname="min_max_normalized_file_progress_vs_iterations_percentage_multi_scatter_plot.pdf")

        multi_scatter_plot_util(iteration_results_df, "iteration_percentage", "percentile_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=scatter_plots_dir, figname="percentile_normalized_file_progress_vs_iterations_percentage_multi_scatter_plot.pdf")
        
        multi_scatter_plot_util(iteration_results_df, "iteration_percentage", "global_scaling_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=scatter_plots_dir, figname="global_scaling_normalized_file_progress_vs_iterations_percentage_multi_scatter_plot.pdf")
        
        multi_scatter_plot_util(iteration_results_df, "iteration_percentage", "weighted_normalized_file_progress", "file_path",
                            title="File Progress vs Iteration Percentage", xlabel="Iteration Percentage (%)", ylabel="File Progress",
                                save_path=scatter_plots_dir, figname="weighted_normalized_file_progress_vs_iterations_percentage_multi_scatter_plot.pdf")

    def mendfile_size_vs_file_size_scatter_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "file_size_kb", "mendfile_size_kb", adjust_left=0.2, 
                          title="Mendfile Size vs File Size", xlabel="File Size (KiB)", ylabel="Mendfile Size (KiB)",
                            save_path=scatter_plots_dir, figname="mendfile_size_vs_file_size_scatter_plot.pdf")

    def iterations_vs_file_size_kb_plot(self, source_results_df, scatter_plots_dir): # this only makes sense for the successful cases  [self.source_results_df["success"]]
        scatter_plot_util(source_results_df, "file_size_kb", "iterations", adjust_left=0.2,
                            title="Iterations vs File Size", xlabel="File Size (KiB)", ylabel="Iterations",
                            save_path=scatter_plots_dir, figname="iterations_vs_file_size_kb.pdf")

    def completion_status_plot_individual(self, cmender_report):
        for i, source_result in enumerate(cmender_report["sourceResults"]):
            line_progresses = [iteration["mendResult"]["fileProgress"] for iteration in source_result["mendingIterations"]]

            df = pd.DataFrame({"iteration": range(len(line_progresses)), "line_progress": line_progresses})

            scatter_plot_util(df, "iteration", "line_progress", adjust_left=0.2,
                            title=f"{source_result['sourceFile'][80:]}", xlabel="Iteration", ylabel="Line count",
                            save_path=self.scatter_plots_dir, figname=f"line_progress_vs_iteration_{i}.pdf")

    def timeMs_vs_file_size_kb_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "file_size_kb", "total_time_ms", adjust_left=0.2,
                            title="Time (ms) vs File Size (KiB)", xlabel="File Size (KiB)", ylabel="Time (ms)",
                            save_path=scatter_plots_dir, figname="timeMs_vs_file_size_kb.pdf")

    def timeMs_vs_iterations_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "iterations", "time_ms", adjust_left=0.2, 
                          title="Time (ms) vs Iterations", xlabel="Iterations", ylabel="Time (ms)",
                            save_path=scatter_plots_dir, figname="time_ms_vs_iterations.pdf")

    def file_size_kb_normalized_total_time_ms_vs_iterations_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "iterations", "file_size_kb_normalized_total_time_ms", adjust_left=0.2, 
                          title="File Size (KiB) Normalized Total Time (ms) vs Iterations", xlabel="Iterations", ylabel="File Size (KiB) Normalized Total Time (ms)",
                            save_path=scatter_plots_dir, figname="file_size_kb_normalized_total_time_ms_vs_iterations.pdf")
    
    def file_size_kb_normalized_iterations_vs_file_progress_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "file_size_kb_normalized_iterations", "file_progress", adjust_left=0.2, 
                          title="File Size (KiB) Normalized Iterations vs File Progress", xlabel="File Size (KiB) Normalized Iterations", ylabel="File Progress",
                            save_path=scatter_plots_dir, figname="file_size_kb_normalized_iterations_vs_file_progress.pdf")

    def last_file_offset_vs_file_size_kb_scatter_plot(self, source_results_df, scatter_plots_dir):
        scatter_plot_util(source_results_df, "file_size_kb", "last_file_offset", adjust_left=0.2, 
                          title="Last File Offset vs File Size", xlabel="File Size (KiB)", ylabel="Last File Offset",
                            save_path=scatter_plots_dir, figname="last_file_offset_vs_file_size_kb.pdf")

    def create_plots(self, source_results_df, iteration_results_df, eval_output_dir):
        histograms_dir = os.path.join(eval_output_dir, "hists")
        scatter_plots_dir = os.path.join(eval_output_dir, "scatters")
        line_plots_dir = os.path.join(eval_output_dir, "lines")

        os.makedirs(histograms_dir, exist_ok=True)
        os.makedirs(scatter_plots_dir, exist_ok=True)
        os.makedirs(line_plots_dir, exist_ok=True)

        # Histograms
        self.file_size_histogram(source_results_df, histograms_dir)
        self.file_progress_histogram(source_results_df, histograms_dir)
        self.iteration_count_histogram(source_results_df, histograms_dir)
        self.total_time_ms_histogram(source_results_df, histograms_dir)
        self.diag_exporter_total_time_ratio_histogram(source_results_df, histograms_dir)
        self.mendfile_size_histogram(source_results_df, histograms_dir)

        # Line plots
        self.file_progress_vs_iterations_percentage_multi_line_plot(iteration_results_df, line_plots_dir)
        self.normalized_file_progress_vs_iterations_percentage_multi_line_plot(iteration_results_df, line_plots_dir)

        # Scatter plots
        self.file_progress_vs_iterations_percentage_multi_scatter_plot(iteration_results_df, scatter_plots_dir)
        self.normalized_file_progress_vs_iterations_percentage_multi_scatter_plot(iteration_results_df, scatter_plots_dir)
        self.file_progress_vs_iteration_count_scatter_plot(source_results_df, scatter_plots_dir) # do more iterations mean better file progress?
        self.mendfile_size_vs_file_size_scatter_plot(source_results_df, scatter_plots_dir)
        self.file_size_kb_normalized_total_time_ms_vs_iterations_plot(source_results_df, scatter_plots_dir) # does more iterations mean more time?
        self.timeMs_vs_file_size_kb_plot(source_results_df, scatter_plots_dir) # does more file size mean more time?
        self.iterations_vs_file_size_kb_plot(source_results_df, scatter_plots_dir) # does more file size mean more iterations?
        self.last_file_offset_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir)

        #self.completion_status_plot_individual() # how does the file progress evolve over iterations? does it cycle or converge assymptotically?

    def save_tables(self, project_eval_output_dir, source_results_df, iteration_results_df):
        project_eval_output_tables_dir = os.path.join(project_eval_output_dir, "tables")

        os.makedirs(project_eval_output_tables_dir, exist_ok=True)

        source_results_df.to_csv(os.path.join(project_eval_output_tables_dir, "source_results.csv"), index=False)
        iteration_results_df.to_csv(os.path.join(project_eval_output_tables_dir, "iteration_results.csv"), index=False)

    #def clean_cmender_outputs(self):
    #    shutil.rmtree(self.cmender_output_dir, ignore_errors=True)

    def evaluate(self, cmender_output_dir, eval_output_dir, dataset_dirpath, dataset_projects_info):
        os.makedirs(cmender_output_dir, exist_ok=True)
        os.makedirs(eval_output_dir, exist_ok=True)

        project_aggr_results = []

        all_projects_source_results_df = pd.DataFrame()

        for dataset_project in dataset_projects_info:
            #if dataset_project["name"] != "id-Software/DOOM":
            #if dataset_project["name"] != "OpenVPN/openvpn":
            #if dataset_project["name"] != "git/git":
            #if dataset_project["name"] != "openssl/openssl":
            #if dataset_project["name"] != "torvalds/linux":
            #if dataset_project["name"] != "openssl/openssl" and dataset_project["name"] != "id-Software/DOOM":
            #if dataset_project["name"] == "torvalds/linux" or dataset_project["name"] == "openssl/openssl" or dataset_project["name"] == "id-Software/DOOM":
            #    continue

            project_files = []

            print("Processing project: ", dataset_project["name"], " branch: ", dataset_project["branch"])

            for root, dirs, files in os.walk(os.path.join(dataset_dirpath, dataset_project["name"].replace("/", "_") + "_" + dataset_project["branch"])):
                for file in files:
                    project_files.append(os.path.abspath(os.path.join(root, file)))

            project_cmender_output_dir = os.path.join(cmender_output_dir, dataset_project["name"].replace("/", "_"))

            # TODO uncomment
            self.run_cmender_on_files(project_files, project_cmender_output_dir)

            time.sleep(2)

            cmender_report = self.read_cmender_report(project_cmender_output_dir)

            if not cmender_report:
                print("Could not read cmender result")
                raise Exception("Could not read cmender result")
        
            source_results_df, iteration_results_df = self.analyze_cmender_results(cmender_report, project_cmender_output_dir, dataset_project["name"])

            all_projects_source_results_df = pd.concat([all_projects_source_results_df, source_results_df], ignore_index=True)

            project_aggr_results.append(self.get_aggr_source_results(source_results_df, dataset_project["name"]))

            project_eval_output_dir = os.path.join(eval_output_dir, dataset_project["name"].replace("/", "_"))

            os.makedirs(project_eval_output_dir, exist_ok=True)

            self.save_tables(project_eval_output_dir, source_results_df, iteration_results_df)
            self.create_plots(source_results_df, iteration_results_df, project_eval_output_dir)

        project_aggr_results_df = pd.DataFrame(project_aggr_results, columns=[
                "project",
                "total_count",
                "success_count",
                "unsuccessful_count",
                "fatal_exception_count",

                "success_ratio",
                "unsuccessful_ratio",
                "fatal_exception_ratio",
                "fatal_exception_over_unsuccessful_ratio",

                "total_time_ms_mean",
                "diag_exporter_total_time_ratio_mean",
                "iterations_mean",
                "file_size_kb_mean",
                "mendfile_size_kb_mean",
                "file_progress_mean",

                "total_time_ms_var",
                "diag_exporter_total_time_ratio_var",
                "iterations_var",
                "file_size_kb_var",
                "mendfile_size_kb_var",
                "file_progress_var",

                "const",
                "const_p_value",
                "file_size_kb_coef",
                "file_size_kb_p_value",
                "prsquared",
                "rsquared"
            ])
        
        project_aggr_results_df.loc[len(project_aggr_results_df)] = self.get_aggr_source_results(all_projects_source_results_df, "all")

        all_eval_output_dir = os.path.join(eval_output_dir, "all")
        all_eval_output_tables_dir = os.path.join(all_eval_output_dir, "tables")

        os.makedirs(all_eval_output_dir, exist_ok=True)
        os.makedirs(all_eval_output_tables_dir, exist_ok=True)

        all_projects_source_results_df.to_csv(os.path.join(all_eval_output_tables_dir, "project_source_results.csv"), index=False)
        project_aggr_results_df.to_csv(os.path.join(all_eval_output_tables_dir, "project_aggr_results.csv"), index=False)

        self.create_plots(all_projects_source_results_df, None, all_eval_output_dir)


        '''
        # for each row in the iteration_results_df
        for file_path, group in self.iteration_results_df.groupby("file_path"):
            #print(f"Processing file: {file_path}")
            # Perform operations on each group
            # For example, you can print the first few rows of each group
            #print(group.head())
            speaman_corr = group["file_progress"].corr(group["iteration_percentage"], method="spearman")
            print(f"Spearman correlation for file {file_path}: {speaman_corr}")
        
        X = self.source_results_df['file_size_kb']  # Independent variable (file size)
        X = sm.add_constant(X)  # Adds a constant (intercept) to the model
        y = self.source_results_df['success']  # Dependent variable (success)

        # Fit the logistic regression model
        model = sm.Logit(y, X)
        result = model.fit()

        # Display the results
        print(result.summary())


        #correlation = self.source_results_df.select_dtypes(include=['number']).corr(method="pearson")
        correlation = self.source_results_df[["file_size_kb", "last_file_offset"]].corr(method="pearson")
        # as heatmap
        sns.heatmap(correlation, annot=True)
        plt.title("Correlation Matrix")
        plt.savefig("correlation_matrix.pdf")
        plt.close()

        correlation = self.source_results_df[["file_size_kb", "last_file_offset"]].corr(method="spearman")
        # as heatmap
        sns.heatmap(correlation, annot=True)
        plt.title("Correlation Matrix")
        plt.savefig("correlation_matrix_spearman.pdf")
        plt.close()
        '''


        '''

        correlation = self.source_results_df.select_dtypes(include=['number']).corr(method="pearson")

        # as heatmap
        sns.heatmap(correlation, annot=True)
        plt.title("Correlation Matrix")
        plt.savefig("correlation_matrix.pdf")
        plt.close()
        '''


        #self.clean_cmender_outputs()

if __name__ == '__main__':
    # args
    
    # if len(sys.argv) != 3:
    #    print("Usage: python evaluation.py <cmender_output_dir> <output_dir>")
    # cmender_output_dir = sys.argv[1]
    # output_dir = sys.argv[2]

    dataset_dirpath = "../repos/repos_output/dataset/projects"
    dataset_projects_info_filepath = "../repos/repos_output/dataset/projects_info.json"

    dataset_projects_info = json.load(open(dataset_projects_info_filepath, "r"))

    eval = Evaluation()

    # ../ for the cmender_output_dir because it can slow down the IDEs

    eval.evaluate(cmender_output_dir="../results/cmender_outputs_headers", eval_output_dir="./results/eval_outputs", 
                  dataset_dirpath=dataset_dirpath, dataset_projects_info=dataset_projects_info)
