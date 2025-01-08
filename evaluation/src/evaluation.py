import json
import subprocess
import os
import shutil
import time
import sys
import re

import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec
import numpy as np
import statsmodels.api as sm

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

def violin_plot_util(df, x_column, y_column,
                        s=5.0, color="skyblue", edgecolors="blue", linewidths=0.8, adjust_left=None, cut=2,
                        title=None, xlabel=None, ylabel=None,
                        save_path=None, figname=None):
    plt.figure(figsize=(8, 6))

    sns.violinplot(data=df, x=x_column, y=y_column, hue=y_column, palette="husl", legend=False, orient="h", cut=cut)

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

# Histograms
'''
def file_size_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "file_size_kb", bins=10, title="File Size Histogram", xlabel="File Size (KB)",
                save_path=histograms_dir, figname="file_size_hist.pdf")
'''

def iteration_count_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "iterations", bins=20, title=None, xlabel="Iterations",
                save_path=histograms_dir, figname="iteration_count_hist.pdf")

def file_progress_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "file_progress", bins=10, title=None, xlabel="File Progress",
                save_path=histograms_dir, figname="file_progress_hist.pdf")
    histogram_util(source_results_df, "file_progress_percentage", bins=10, title=None, xlabel="File Progress (%)",
            save_path=histograms_dir, figname="file_progress_percentage_hist.pdf")

def total_time_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "total_time_ms", bins=20, title=None, xlabel="Total Time (ms)",
                save_path=histograms_dir, figname="total_time_ms_hist.pdf")

    histogram_util(source_results_df, "total_time_secs", bins=20, title=None, xlabel="Total Time (s)",
                save_path=histograms_dir, figname="total_time_secs_hist.pdf")

def diag_exporter_total_time_ratio_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "diag_exporter_total_time_ratio", bins=20, title=None, xlabel="diag-exporter Total Time Ratio",
                save_path=histograms_dir, figname="diag_exporter_total_time_ratio_hist.pdf")

def total_time_without_diag_exporter_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "total_time_ms_without_diag_exporter", bins=20, title=None, xlabel="Total Time without diag-exporter (ms)",
                save_path=histograms_dir, figname="total_time_ms_without_diag_exporter_hist.pdf")

    histogram_util(source_results_df, "total_time_secs_without_diag_exporter", bins=20, title=None, xlabel="Total Time without diag-exporter (s)",
                save_path=histograms_dir, figname="total_time_secs_without_diag_exporter_hist.pdf")

def total_time_per_iteration_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "total_time_ms_per_iteration", bins=20, title=None, xlabel="Total Time per Iteration (ms)",
                save_path=histograms_dir, figname="total_time_ms_per_iteration_hist.pdf")
    
    histogram_util(source_results_df, "total_time_secs_per_iteration", bins=20, title=None, xlabel="Total Time per Iteration (s)",
            save_path=histograms_dir, figname="total_time_secs_per_iteration_hist.pdf")

def mendfile_size_histogram(source_results_df, histograms_dir):
    histogram_util(source_results_df, "mendfile_size_kb", bins=20, title=None, xlabel="Mendfile Size (KB)",
                save_path=histograms_dir, figname="mendfile_size_hist.pdf")

# Violin plots
'''
def file_size_violin_plot(source_results_df, violin_plots_dir):
    sns.violinplot(data=source_results_df["file_size_kb"], color="skyblue")
    plt.title("File Size Violin Plot")
    plt.xlabel("File Size (KB)")

    plt.savefig(os.path.join(violin_plots_dir, "file_size_violin_plot.pdf"))
    plt.close()
'''

def iteration_count_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "iterations", "project", title=None, xlabel="Iterations",
                save_path=violin_plots_dir, figname="iteration_count_violin_plot.pdf")

def file_progress_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "file_progress", "project", title=None, xlabel="File Progress",
                     cut=0, save_path=violin_plots_dir, figname="file_progress_violin_plot.pdf")

def total_time_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "total_time_secs", "project", title=None, xlabel="Total Time (s)",
                save_path=violin_plots_dir, figname="total_time_secs_violin_plot.pdf")

def diag_exporter_total_time_ratio_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "diag_exporter_total_time_ratio", "project", title=None, xlabel="Diag-exporter Total Time Ratio",
                cut=0, save_path=violin_plots_dir, figname="diag_exporter_total_time_ratio_violin_plot.pdf")

def total_time_without_diag_exporter_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "total_time_secs_without_diag_exporter", "project", title=None, xlabel="Total Time without Diag-exporter Time (s)",
                save_path=violin_plots_dir, figname="total_time_secs_without_diag_exporter_violin_plot.pdf")

def total_time_per_iteration_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "total_time_secs_per_iteration", "project", title=None, xlabel="Total Time per Iteration (s)",
                save_path=violin_plots_dir, figname="total_time_secs_per_iteration_violin_plot.pdf")

def total_time_without_diag_exporter_per_iteration_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "total_time_secs_without_diag_exporter_per_iteration", "project", title=None, xlabel="Total Time without diag-exporter per Iteration (s)",
                save_path=violin_plots_dir, figname="total_time_secs_without_diag_exporter_per_iteration_violin_plot.pdf")

def mendfile_size_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "mendfile_size_kb", "project", title=None, xlabel="Mendfile Size (KB)",
                save_path=violin_plots_dir, figname="mendfile_size_violin_plot.pdf")

def spearman_corr_file_progress_iterations_ratio_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "spearman_corr_file_progress_iterations_ratio", "project", title=None, xlabel="SRCC(File Progress, Iterations Ratio)",
                cut=0, save_path=violin_plots_dir, figname="spearman_corr_file_progress_iterations_ratio_violin_plot.pdf")
    
def pearson_corr_file_progress_accum_total_time_ms_violin_plot(source_results_df, violin_plots_dir):
    violin_plot_util(source_results_df, "pearson_corr_file_progress_accum_total_time_ms", "project", title=None, xlabel="PCC(File Progress, Accumulated Total Time (ms))",
                cut=0, save_path=violin_plots_dir, figname="pearson_corr_file_progress_accum_total_time_ms_violin_plot.pdf")

def file_progress_multiple_related_violin_plots(source_results_df, violins_plots_dir):
    #fig, axes = plt.subplots(2, 2, figsize=(12, 10))
    fig = plt.figure(figsize=(10, 8))

    gs = gridspec.GridSpec(2, 2)
    # First row: two plots
    ax1 = fig.add_subplot(gs[0, 0])  # Top-left
    ax2 = fig.add_subplot(gs[0, 1])  # Top-right

    # Second row: one centered plot
    ax3 = fig.add_subplot(gs[1, 0]) 

    # Plot each violin plot
    sns.violinplot(data=source_results_df, x="file_progress", y="project", hue="project", palette="husl", legend=False, orient="h", cut=0, ax=ax1)
    ax1.set_title("File Progress")
    ax1.set_xlabel("")
    ax1.set_ylabel("")

    sns.violinplot(data=source_results_df, x="spearman_corr_file_progress_iterations_ratio", y="project", hue="project", palette="husl", legend=False, orient="h", cut=0, ax=ax2)
    ax2.set_title("SRCC(File Progress, Iterations Ratio)")
    ax2.set_xlabel("")
    ax2.set_ylabel("")

    sns.violinplot(data=source_results_df, x="pearson_corr_file_progress_accum_total_time_ms", y="project", hue="project", palette="husl", legend=False, orient="h", cut=0, ax=ax3)
    ax3.set_title("PCC(File Progress, Accumulated Total Time (ms))")
    ax3.set_xlabel("")
    ax3.set_ylabel("")

    # Adjust layout for better appearance
    plt.tight_layout()

    plt.savefig(os.path.join(violins_plots_dir, "file_progress_related_violin_plots.pdf"))
    plt.close()

def time_multiple_related_violin_plots(source_results_df, violins_plots_dir):
   #fig, axes = plt.subplots(2, 2, figsize=(12, 10))
    fig = plt.figure(figsize=(10, 8))

    gs = gridspec.GridSpec(2, 2)
    # First row: two plots
    ax1 = fig.add_subplot(gs[0, 0])  # Top-left
    ax2 = fig.add_subplot(gs[0, 1])  # Top-right

    # Second row: one centered plot
    ax3 = fig.add_subplot(gs[1, 0]) 

    # Plot each violin plot
    sns.violinplot(data=source_results_df, x="total_time_secs", y="project", hue="project", palette="husl", legend=False, orient="h", ax=ax1)
    ax1.set_title("Total Time (s)")
    ax1.set_xlabel("")
    ax1.set_ylabel("")

    sns.violinplot(data=source_results_df, x="total_time_secs_per_iteration", y="project", hue="project", palette="husl", legend=False, orient="h", ax=ax2)
    ax2.set_title("Total Time per Iteration (s)")
    ax2.set_xlabel("")
    ax2.set_ylabel("")

    sns.violinplot(data=source_results_df, x="diag_exporter_total_time_ratio", y="project", hue="project", palette="husl", legend=False, orient="h", cut=0, ax=ax3)
    ax3.set_title("Diag-exporter Total Time Ratio")
    ax3.set_xlabel("")
    ax3.set_ylabel("")

    # Adjust layout for better appearance
    plt.tight_layout()

    plt.savefig(os.path.join(violins_plots_dir, "time_related_violin_plots.pdf"))
    plt.close()


# Line plots


# Scatter plots
def file_progress_vs_iteration_count_scatter_plot(source_results_df, scatter_plots_dir):
    scatter_plot_util(source_results_df, "iterations", "file_progress", adjust_left=0.2, 
                        title=None, xlabel="Iterations", ylabel="File Progress",
                        save_path=scatter_plots_dir, figname="file_progress_vs_iterations_scatter_plot.pdf")

def iterations_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir): # this only makes sense for the successful cases  [source_results_df["success"]]
    scatter_plot_util(source_results_df, "file_size_kb", "iterations", adjust_left=0.2,
                        title=None, xlabel="File Size (KB)", ylabel="Iterations",
                        save_path=scatter_plots_dir, figname="iterations_vs_file_size_kb.pdf")

def iterations_normalized_by_file_size_kb_vs_file_progress_plot(source_results_df, scatter_plots_dir):
    scatter_plot_util(source_results_df, "file_progress", "iteration_num_normalized_by_file_size_kb", adjust_left=0.2, 
                        title=None, xlabel="File Progress", ylabel="Iterations / File Size (/KB)",
                        save_path=scatter_plots_dir, figname="iterations_normalized_by_file_size_kb_vs_file_progress.pdf")

def mendfile_size_kb_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir):
    scatter_plot_util(source_results_df, "file_size_kb", "mendfile_size_kb", adjust_left=0.2, 
                        title=None, xlabel="File Size (KB)", ylabel="Mendfile Size (KB)",
                        save_path=scatter_plots_dir, figname="mendfile_size_vs_file_size_scatter_plot.pdf")

def mendfile_size_kb_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir):
    scatter_plot_util(source_results_df, "iterations", "mendfile_size_kb", adjust_left=0.2, 
                        title=None, xlabel="Iterations", ylabel="Mendfile Size (KB)",
                        save_path=scatter_plots_dir, figname="mendfile_size_vs_iterations_scatter_plot.pdf")

def mendfile_size_kb_vs_total_time_secs_scatter_plot(source_results_df, scatter_plots_dir):
    scatter_plot_util(source_results_df, "total_time_secs", "mendfile_size_kb", adjust_left=0.2, 
                        title=None, xlabel="Total Time (s)", ylabel="Mendfile Size (KB)",
                        save_path=scatter_plots_dir, figname="mendfile_size_vs_total_time_secs_scatter_plot.pdf")

def completion_status_plot_individual(cmender_report):
    for i, source_result in enumerate(cmender_report["sourceResults"]):
        line_progresses = [iteration["mendResult"]["fileProgress"] for iteration in source_result["mendingIterations"]]

        df = pd.DataFrame({"iteration": range(len(line_progresses)), "line_progress": line_progresses})

        scatter_plot_util(df, "iteration", "line_progress", adjust_left=0.2,
                        title=f"{source_result['sourceFile'][80:]}", xlabel="Iteration", ylabel="Line count",
                        save_path=scatter_plots_dir, figname=f"line_progress_vs_iteration_{i}.pdf")

def total_time_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir):
    #scatter_plot_util(source_results_df, "file_size_kb", "total_time_ms", adjust_left=0.2,
    #                    title=None, xlabel="File Size (KB)", ylabel="Time (ms)",
    #                    save_path=scatter_plots_dir, figname="time_ms_vs_file_size_kb.pdf")
    scatter_plot_util(source_results_df, "file_size_kb", "total_time_secs", adjust_left=0.2,
                        title=None, xlabel="File Size (KB)", ylabel="Time (s)",
                        save_path=scatter_plots_dir, figname="total_time_secs_vs_file_size_kb.pdf")

def total_time_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir):
    #scatter_plot_util(source_results_df, "iterations", "total_time_ms", adjust_left=0.2, 
    #                    title=None, xlabel="Iterations", ylabel="Time (ms)",
    #                    save_path=scatter_plots_dir, figname="total_time_ms_vs_iterations.pdf")
    scatter_plot_util(source_results_df, "iterations", "total_time_secs", adjust_left=0.2,
                        title=None, xlabel="Iterations", ylabel="Time (s)",
                        save_path=scatter_plots_dir, figname="total_time_secs_vs_iterations.pdf")

def total_time_normalized_by_file_size_kb_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir):
    #scatter_plot_util(source_results_df, "iterations", "total_time_ms_normalized_by_file_size_kb", adjust_left=0.2, 
    #                    title=None, xlabel="Iterations", ylabel="Total Time / File Size (ms/KB)",
    #                    save_path=scatter_plots_dir, figname="total_time_ms_normalized_by_file_size_kb_vs_iterations.pdf")
    
    scatter_plot_util(source_results_df, "iterations", "total_time_secs_normalized_by_file_size_kb", adjust_left=0.2,
                        title=None, xlabel="Iterations", ylabel="Total Time / File Size (s/KB)",
                        save_path=scatter_plots_dir, figname="total_time_secs_normalized_by_file_size_kb_vs_iterations.pdf")

def total_time_without_diag_exporter_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir):
    #scatter_plot_util(source_results_df, "file_size_kb", "total_time_ms_without_diag_exporter", adjust_left=0.2, 
    #                    title=None, xlabel="File Size (KB)", ylabel="Total Time without diag-exporter (ms)",
    #                    save_path=scatter_plots_dir, figname="total_time_ms_without_diag_exporter_vs_file_size_kb.pdf")
    
    scatter_plot_util(source_results_df, "file_size_kb", "total_time_secs_without_diag_exporter", adjust_left=0.2,
                        title=None, xlabel="File Size (KB)", ylabel="Total Time without diag-exporter (s)",
                        save_path=scatter_plots_dir, figname="total_time_secs_without_diag_exporter_vs_file_size_kb.pdf")

def total_time_without_diag_exporter_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir):
    #scatter_plot_util(source_results_df, "iterations", "total_time_ms_without_diag_exporter", adjust_left=0.2, 
    #                    title=None, xlabel="Iterations", ylabel="Total Time without diag-exporter (ms)",
    #                    save_path=scatter_plots_dir, figname="total_time_ms_without_diag_exporter_vs_iterations.pdf")
    
    scatter_plot_util(source_results_df, "iterations", "total_time_secs_without_diag_exporter", adjust_left=0.2,
                        title=None, xlabel="Iterations", ylabel="Total Time without diag-exporter (s)",
                        save_path=scatter_plots_dir, figname="total_time_secs_without_diag_exporter_vs_iterations.pdf")

def create_plots(source_results_df, iteration_results_df, eval_output_dir):
    histograms_dir = os.path.join(eval_output_dir, "hists")
    scatter_plots_dir = os.path.join(eval_output_dir, "scatters")
    line_plots_dir = os.path.join(eval_output_dir, "lines")
    violins_plots_dir = os.path.join(eval_output_dir, "violins")

    os.makedirs(histograms_dir, exist_ok=True)
    os.makedirs(scatter_plots_dir, exist_ok=True)
    os.makedirs(line_plots_dir, exist_ok=True)
    os.makedirs(violins_plots_dir, exist_ok=True)


    # Histograms
    #file_size_histogram(source_results_df, histograms_dir)
    iteration_count_histogram(source_results_df, histograms_dir)
    file_progress_histogram(source_results_df, histograms_dir)
    total_time_histogram(source_results_df, histograms_dir)
    diag_exporter_total_time_ratio_histogram(source_results_df, histograms_dir)
    total_time_without_diag_exporter_histogram(source_results_df, histograms_dir)
    total_time_per_iteration_histogram(source_results_df, histograms_dir)
    mendfile_size_histogram(source_results_df, histograms_dir)


    # Violin plots
    #file_size_violin_plot(source_results_df, violins_plots_dir)
    iteration_count_violin_plot(source_results_df, violins_plots_dir)
    file_progress_violin_plot(source_results_df, violins_plots_dir)
    total_time_violin_plot(source_results_df, violins_plots_dir)
    diag_exporter_total_time_ratio_violin_plot(source_results_df, violins_plots_dir)
    total_time_without_diag_exporter_violin_plot(source_results_df, violins_plots_dir)
    total_time_per_iteration_violin_plot(source_results_df, violins_plots_dir)
    total_time_without_diag_exporter_per_iteration_violin_plot(source_results_df, violins_plots_dir)
    mendfile_size_violin_plot(source_results_df, violins_plots_dir)
    spearman_corr_file_progress_iterations_ratio_violin_plot(source_results_df, violins_plots_dir)
    pearson_corr_file_progress_accum_total_time_ms_violin_plot(source_results_df, violins_plots_dir)
    file_progress_multiple_related_violin_plots(source_results_df, violins_plots_dir)
    time_multiple_related_violin_plots(source_results_df, violins_plots_dir)

    # Line plots

    # Scatter plots
    #completion_status_plot_individual() # how does the file progress evolve over iterations? does it cycle or converge assymptotically?
    file_progress_vs_iteration_count_scatter_plot(source_results_df, scatter_plots_dir) # do more iterations mean better file progress?
    mendfile_size_kb_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir)
    mendfile_size_kb_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir)
    mendfile_size_kb_vs_total_time_secs_scatter_plot(source_results_df, scatter_plots_dir)
    iterations_normalized_by_file_size_kb_vs_file_progress_plot(source_results_df, scatter_plots_dir) # does more file size mean more iterations?
    iterations_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir) # does more file size mean more iterations?
    total_time_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir) # does more file size mean more time?
    total_time_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir) # does more iterations mean more time?
    total_time_normalized_by_file_size_kb_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir) # does more iterations mean more time?
    total_time_without_diag_exporter_vs_file_size_kb_scatter_plot(source_results_df, scatter_plots_dir) # does more file size mean more time?
    total_time_without_diag_exporter_vs_iterations_scatter_plot(source_results_df, scatter_plots_dir) # does more iterations mean more time?

    correlation_matrix = source_results_df[[
        "file_size_kb",
        "iterations",
        "file_progress",
        "total_time_secs",
        "diag_exporter_total_time_ratio",
        "total_time_secs_without_diag_exporter",
        "total_time_secs_per_iteration",
        "total_time_secs_without_diag_exporter_per_iteration",
        "total_time_secs_normalized_by_file_size_kb",
        "iteration_num_normalized_by_file_size_kb",
        "mendfile_size_kb",
    ]].corr()

    plt.figure(figsize=(25, 25))
    sns.heatmap(correlation_matrix, annot=True, cmap="coolwarm", linewidths=0.5)
    plt.title("Correlation Matrix Heatmap")
    plt.savefig(os.path.join(scatter_plots_dir, "correlation_matrix_heatmap.pdf"))
    plt.close()


    '''
    # Line plots
    file_progress_vs_iterations_percentage_multi_line_plot(iteration_results_df, line_plots_dir)
    normalized_file_progress_vs_iterations_percentage_multi_line_plot(iteration_results_df, line_plots_dir)

    '''
    #completion_status_plot_individual() # how does the file progress evolve over iterations? does it cycle or converge assymptotically?

def save_tables(project_eval_output_dir, source_results_df, iteration_results_df):
    project_eval_output_tables_dir = os.path.join(project_eval_output_dir, "tables")

    os.makedirs(project_eval_output_tables_dir, exist_ok=True)

    source_results_df.to_csv(os.path.join(project_eval_output_tables_dir, "source_results.csv"), index=False)
    iteration_results_df.to_csv(os.path.join(project_eval_output_tables_dir, "iteration_results.csv"), index=False)

def read_cmender_report(cmender_output_dir):
    try:
        with open(f"{cmender_output_dir}/cmender_report.json") as result_file:
            return json.load(result_file)
    except Exception as e:
        print("Exception while reading cmender_report: ", e)
        return None

def read_cmender_source_report(source_report_path):
    try:
        with open(f"{source_report_path}/source_report.json") as result_file:
            source_report = json.load(result_file)
        return source_report
    except Exception as e:
        print("Exception while reading source_report: ", e)
        return None

def analyze_project_results(cmender_report, cmender_output_dir, project_name):
    source_results_data = [None] * cmender_report["totalFiles"]

    i = 0

    iteration_results_df = pd.DataFrame(columns=[
            "project",
            "file_path",
            "iteration_num",
            "iteration_ratio",
            "iteration_percentage",
            "file_progress",
            "file_progress_ratio",
            "accum_total_time_ms"
        ])

    for source_report_dirname in os.listdir(cmender_output_dir):
        source_report_path = os.path.join(cmender_output_dir, source_report_dirname)

        if not os.path.isdir(source_report_path):
            continue

        source_report = read_cmender_source_report(source_report_path)

        if source_report is None:
            print("Could not read source report for item: ", source_report_dirname)
            continue

        fatal_exception = source_report["fatalException"]

        if fatal_exception is not None:
            print("Fatal exception: ", fatal_exception["message"], " for file: ", source_report["sourceFile"][80:])
            #pass

        #if fatal_exception is not None and "could not create mending dir" in fatal_exception["message"]:
        #    print("Fatal exception: ", fatal_exception, " for file: ", source_report["sourceFile"])

        iteration_count = source_report["iterationCount"]
        #print("Iteration count: ", iteration_count, " for file: ", source_result["sourceFile"])

        time_accum_ms = 0

        for j, mending_iteration in enumerate(source_report["mendingIterations"]):
            #print("Iteration: ", j+1, " for file: ", source_report["sourceFile"][80:], " with file progress: ", mending_iteration["terminationStatus"]["fileProgress"])

            time_accum_ms += mending_iteration["totalTime"]["millis"]

            iteration_results_df.loc[len(iteration_results_df)] = (
                source_report_dirname, # project
                source_report["sourceFile"], # file_path
                j+1, # iteration number
                (j+1) / iteration_count, # iteration_ratio
                (j+1) * 100 / iteration_count, # iteration_percentage (normalized)
                mending_iteration["terminationStatus"]["fileProgress"], # file progress (0-1)
                mending_iteration["terminationStatus"]["fileProgress"] * 100, # file progress (0-100)
                time_accum_ms, # accum_total_time_ms
            )

        source_results_data[i] = (
            project_name, # project
            source_report["sourceFile"].replace("/Users/ctw03468/Desktop/tese", "/home/specs/Desktop/pedrojsilva"), # file_path
            #source_report["sourceFile"], # file_path

            source_report["fileSize"]["bytes"], # file_size_b
            source_report["fileSize"]["kilobytes"], # file_size_kb

            source_report["success"], # success
            source_report["fatalException"] is not None, # fatal_exception

            source_report["iterationCount"], # iterations

            source_report["completionStatusEstimate"], # file_progress
            source_report["completionStatusEstimate"] * 100, # file_progress_percentage

            source_report["totalTime"]["millis"], # total_time_ms
            source_report["totalTime"]["seconds"], # total_time_secs

            source_report["diagExporterTotalTime"]["ratio"], # diag_exporter_total_time_ratio

            source_report["totalTime"]["millis"] - source_report["totalTime"]["millis"] * source_report["diagExporterTotalTime"]["ratio"], # total_time_ms_without_diag_exporter
            source_report["totalTime"]["seconds"] - source_report["totalTime"]["seconds"] * source_report["diagExporterTotalTime"]["ratio"], # total_time_secs_without_diag_exporter

            source_report["totalTime"]["millis"] / source_report["iterationCount"], # total_time_ms_per_iteration
            source_report["totalTime"]["seconds"] / source_report["iterationCount"], # total_time_secs_per_iteration

            (source_report["totalTime"]["millis"] - source_report["totalTime"]["millis"] * source_report["diagExporterTotalTime"]["ratio"]) / source_report["iterationCount"], # total_time_ms_without_diag_exporter_per_iteration
            (source_report["totalTime"]["seconds"] - source_report["totalTime"]["seconds"] * source_report["diagExporterTotalTime"]["ratio"]) / source_report["iterationCount"], # total_time_secs_without_diag_exporter_per_iteration

            source_report["totalTime"]["millis"] / source_report["fileSize"]["kilobytes"], # total_time_ms_normalized_by_file_size_kb
            source_report["totalTime"]["seconds"] / source_report["fileSize"]["kilobytes"], # total_time_secs_normalized_by_file_size_kb

            source_report["iterationCount"] / source_report["fileSize"]["kilobytes"], # iteration_num_normalized_by_file_size_kb

            source_report["mendfileSize"]["bytes"], # mendfile_size_b
            source_report["mendfileSize"]["kilobytes"], # mendfile_size_kb

            iteration_results_df[iteration_results_df["file_path"] == source_report["sourceFile"]][["file_progress", "iteration_ratio"]].corr(method="spearman").iloc[0, 1], # spearman_corr_file_progress_iterations_ratio
            iteration_results_df[iteration_results_df["file_path"] == source_report["sourceFile"]][["file_progress", "accum_total_time_ms"]].corr(method="pearson").iloc[0, 1], # pearson_corr_file_progress_accum_total_time_ms
        )
#Iteracoes por sucesso
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

        "file_progress",
        "file_progress_percentage",

        "total_time_ms",
        "total_time_secs",

        "diag_exporter_total_time_ratio",

        "total_time_ms_without_diag_exporter",
        "total_time_secs_without_diag_exporter",

        "total_time_ms_per_iteration",
        "total_time_secs_per_iteration",

        "total_time_ms_without_diag_exporter_per_iteration",
        "total_time_secs_without_diag_exporter_per_iteration",

        "total_time_ms_normalized_by_file_size_kb",
        "total_time_secs_normalized_by_file_size_kb",

        "iteration_num_normalized_by_file_size_kb",

        "mendfile_size_b",
        "mendfile_size_kb",

        "spearman_corr_file_progress_iterations_ratio",
        "pearson_corr_file_progress_accum_total_time_ms",
    ])

    return source_results_df, iteration_results_df

def get_aggr_source_results(source_results_df, project_name):
    return (
            project_name, # project
            source_results_df["file_size_kb"].mean(), # file_size_kb_mean
            source_results_df["file_size_kb"].var(), # file_size_kb_var
            
            len(source_results_df), # total_count
            source_results_df["success"].sum(), # success_count
            len(source_results_df) - source_results_df["success"].sum(), # unsuccessful_count
            source_results_df["fatal_exception"].sum(), # fatal_exception_count

            source_results_df["success"].sum() / len(source_results_df), # success_ratio
            (len(source_results_df) - source_results_df["success"].sum()) / len(source_results_df), # unsuccessful_ratio
            source_results_df["fatal_exception"].sum() / len(source_results_df), # fatal_exception_ratio
            0 if (len(source_results_df) - source_results_df["success"].sum()) == 0 else source_results_df["fatal_exception"].sum() / (len(source_results_df) - source_results_df["success"].sum()), # fatal_exception_over_unsuccessful_ratio

            source_results_df["iterations"].mean(), # iterations_mean
            source_results_df["iterations"].var(), # iterations_var

            source_results_df["file_progress"].mean(), # file_progress_mean
            source_results_df["file_progress"].var(), # file_progress_var

            source_results_df["total_time_secs"].mean(), # total_time_secs_mean
            source_results_df["total_time_secs"].var(), # total_time_secs_var

            source_results_df["diag_exporter_total_time_ratio"].mean(), # diag_exporter_total_time_ratio_mean
            source_results_df["diag_exporter_total_time_ratio"].var(), # diag_exporter_total_time_ratio_var

            source_results_df["total_time_secs_per_iteration"].mean(), # total_time_secs_per_iteration_mean
            source_results_df["total_time_secs_per_iteration"].var(), # total_time_secs_per_iteration_var

            source_results_df["mendfile_size_kb"].mean(), # mendfile_size_kb_mean
            source_results_df["mendfile_size_kb"].var(), # mendfile_size_kb_var

            # TU-Patcher
            source_results_df["tupatcher_success"].sum() / len(source_results_df), # tupatcher_success_ratio
            1 - source_results_df["tupatcher_success"].sum() / len(source_results_df), # tupatcher_unsuccessful_ratio

            source_results_df["tupatcher_iterations"].mean(), # tupatcher_iterations_mean
            source_results_df["tupatcher_iterations"].var(), # tupatcher_iterations_var

            source_results_df["tupatcher_file_progress"].mean(), # tupatcher_file_progress_mean
            source_results_df["tupatcher_file_progress"].var(), # tupatcher_file_progress_var

            source_results_df["tupatcher_total_time_secs"].mean(), # tupatcher_total_time_secs_mean
            source_results_df["tupatcher_total_time_secs"].var(), # tupatcher_total_time_secs_var

            source_results_df["tupatcher_total_time_secs_per_iteration"].mean(), # tupatcher_total_time_secs_per_iteration_mean
            source_results_df["tupatcher_total_time_secs_per_iteration"].var(), # tupatcher_total_time_secs_per_iteration_var,

            source_results_df["tupatcher_max_iterations_reached"].sum() / len(source_results_df), # tupatcher_max_iteration_reached_ratio
        )

def calculate_file_progress(file_path, includes_path, tupatcher_output_dir):
    return 1.0 # TODO remove this line
    print("Calculating file progress for file: ", file_path)
    filename_no_ext = os.path.splitext(os.path.basename(file_path))[0]
    
    patched_filepath = os.path.join(tupatcher_output_dir, filename_no_ext + "_patched.c")
    header_filepath = os.path.join(tupatcher_output_dir, filename_no_ext + "_patched.h")

    try:
        result = subprocess.run(
            ["clang", "-x", "c++", "-fsyntax-only", "-ferror-limit=0", 
                "-nostdinc", "-isysroot", "\"\"", "-I" + includes_path, "-I"+header_filepath, patched_filepath],
            stdout=subprocess.PIPE,  # Suppress stdout
            stderr=subprocess.PIPE   # Suppress stderr
        )

        if result.returncode == 0:
            return 1.0
        
        error_line = re.search(r"patched.c:(\d+):(\d+): error:", result.stderr.decode("utf-8"))

        if error_line is None:
            print("Result: ", result.stderr[:1000])

        row = int(error_line.group(1))
        column = int(error_line.group(2))

        file_size_bytes = os.path.getsize(patched_filepath)

        total_bytes = 0

        with open(patched_filepath, 'r', encoding='utf-8') as file:
            for current_row, line in enumerate(file, start=1):
                if current_row < row:
                    total_bytes += len(line.encode('utf-8'))
                elif current_row == row:
                    # Add up to the target column for the final row
                    total_bytes += len(line[:column].encode('utf-8'))
                    break
                else:
                    break

        return total_bytes / file_size_bytes
        
    except Exception as e:
        raise e

def prepare_tupatcher_sources_df(tupatcher_output_dir, includes_path):
    tupatcher_df = pd.read_csv(os.path.join(tupatcher_output_dir, "tu_patcher_stats.csv"), sep=";")

    # rename columns
    tupatcher_df = tupatcher_df.rename(columns={
        "File": "file_path",
        "Success": "tupatcher_success",
        "Iterations": "tupatcher_iterations",
        "Execution Time (ns)": "tupatcher_total_time_ns",
    })

    # add tupatcher_total_time_secs
    tupatcher_df["tupatcher_total_time_secs"] = tupatcher_df["tupatcher_total_time_ns"] / 1e9

    # add tupatcher_total_time_secs_per_iteration
    tupatcher_df["tupatcher_total_time_secs_per_iteration"] = tupatcher_df["tupatcher_total_time_secs"] / tupatcher_df["tupatcher_iterations"]

    # remove execution time column
    tupatcher_df = tupatcher_df.drop(columns=["tupatcher_total_time_ns"])

    # apply calculate_file_progress for each file
    tupatcher_df["tupatcher_file_progress"] = tupatcher_df.apply(lambda row: calculate_file_progress(row["file_path"], includes_path, tupatcher_output_dir), axis=1)

    tupatcher_df["tupatcher_max_iterations_reached"] = tupatcher_df.apply(lambda row: (row["tupatcher_iterations"] == 100) & (row["tupatcher_success"] == False), axis=1)

    return tupatcher_df

def evaluate(cmender_output_dir, tupatcher_output_dir, eval_output_dir, dataset_projects_info, includes_path):
    tupatcher_df = prepare_tupatcher_sources_df(tupatcher_output_dir, includes_path)

    os.makedirs(eval_output_dir, exist_ok=True)

    project_aggr_results = []

    all_projects_source_results_df = pd.DataFrame()

    project_map = {
        "torvalds/linux": "Linux",
        "git/git": "Git",
        "openssl/openssl": "OpenSSL",
        "OpenVPN/openvpn": "OpenVPN",
        "id-Software/DOOM": "DOOM",
    }

    for dataset_project in dataset_projects_info:
        project_cmender_output_dir = os.path.join(cmender_output_dir, dataset_project["name"].replace("/", "_"))

        cmender_report = read_cmender_report(project_cmender_output_dir)

        if not cmender_report:
            print("Could not read cmender result")
            raise Exception("Could not read cmender result")
    
        source_results_df, iteration_results_df = analyze_project_results(cmender_report, project_cmender_output_dir, project_map[dataset_project["name"]])
    
        source_results_df = pd.merge(source_results_df, tupatcher_df, on="file_path", how="left")

        all_projects_source_results_df = pd.concat([all_projects_source_results_df, source_results_df], ignore_index=True)

        project_aggr_results.append(get_aggr_source_results(source_results_df, dataset_project["name"]))

        project_eval_output_dir = os.path.join(eval_output_dir, dataset_project["name"].replace("/", "_"))

        os.makedirs(project_eval_output_dir, exist_ok=True)

        save_tables(project_eval_output_dir, source_results_df, iteration_results_df)
        create_plots(source_results_df, iteration_results_df, project_eval_output_dir)
    
    project_aggr_results_df = pd.DataFrame(project_aggr_results, columns=[
            "project",
            "file_size_kb_mean",
            "file_size_kb_var",

            "total_count",
            "success_count",
            "unsuccessful_count",
            "fatal_exception_count",

            "success_ratio",
            "unsuccessful_ratio",
            "fatal_exception_ratio",
            "fatal_exception_over_unsuccessful_ratio",

            "iterations_mean",
            "iterations_var",

            "file_progress_mean",
            "file_progress_var",

            "total_time_secs_mean",
            "total_time_secs_var",

            "diag_exporter_total_time_ratio_mean",
            "diag_exporter_total_time_ratio_var",

            "total_time_secs_per_iteration_mean",
            "total_time_secs_per_iteration_var",

            "mendfile_size_kb_mean",
            "mendfile_size_kb_var",

            # TU-Patcher
            "tupatcher_success_ratio",
            "tupatcher_unsuccessful_ratio",

            "tupatcher_iterations_mean",
            "tupatcher_iterations_var",

            "tupatcher_file_progress_mean",
            "tupatcher_file_progress_var",

            "tupatcher_total_time_secs_mean",
            "tupatcher_total_time_secs_var",

            "tupatcher_total_time_secs_per_iteration_mean",
            "tupatcher_total_time_secs_per_iteration_var",

            "tupatcher_max_iterations_reached_ratio",
        ])
            
    project_aggr_results_df.loc[len(project_aggr_results_df)] = get_aggr_source_results(all_projects_source_results_df, "all")

    all_eval_output_dir = os.path.join(eval_output_dir, "all")
    all_eval_output_tables_dir = os.path.join(all_eval_output_dir, "tables")

    os.makedirs(all_eval_output_dir, exist_ok=True)
    os.makedirs(all_eval_output_tables_dir, exist_ok=True)

    all_projects_source_results_df.to_csv(os.path.join(all_eval_output_tables_dir, "project_source_results.csv"), index=False)
    project_aggr_results_df.to_csv(os.path.join(all_eval_output_tables_dir, "project_aggr_results.csv"), index=False)

    concise_project_aggr_results_df = project_aggr_results_df[
        [
            "project",
            "success_ratio", "tupatcher_success_ratio",
            "unsuccessful_ratio", "tupatcher_unsuccessful_ratio",
            "iterations_mean", "tupatcher_iterations_mean",
            "iterations_var", "tupatcher_iterations_var",
            "total_time_secs_mean", "tupatcher_total_time_secs_mean",
            "total_time_secs_var", "tupatcher_total_time_secs_var",
            "total_time_secs_per_iteration_mean", "tupatcher_total_time_secs_per_iteration_mean",
            "total_time_secs_per_iteration_var", "tupatcher_total_time_secs_per_iteration_var",
            "tupatcher_max_iterations_reached_ratio"
        ]]

    concise_project_aggr_results_df.to_csv(os.path.join(all_eval_output_tables_dir, "concise_project_aggr_results.csv"), index=False)

    create_plots(all_projects_source_results_df, None, all_eval_output_dir)

    cmender_upgrade_df = all_projects_source_results_df[(all_projects_source_results_df["success"] == True) & (all_projects_source_results_df["tupatcher_success"] == False)]
    cmender_downgrade_df = all_projects_source_results_df[(all_projects_source_results_df["success"] == False) & (all_projects_source_results_df["tupatcher_success"] == True)]
    tupatcher_max_iterations_df = all_projects_source_results_df[all_projects_source_results_df["tupatcher_max_iterations_reached"] == True]

    cmender_upgrade_df.to_csv(os.path.join(all_eval_output_tables_dir, "cmender_upgrade.csv"), index=False)
    cmender_downgrade_df.to_csv(os.path.join(all_eval_output_tables_dir, "cmender_downgrade.csv"), index=False)
    tupatcher_max_iterations_df.to_csv(os.path.join(all_eval_output_tables_dir, "tupatcher_max_iterations.csv"), index=False)

    # save tables in latex
    latex_table = concise_project_aggr_results_df.to_latex(index=False)

    os.makedirs(os.path.join(all_eval_output_tables_dir, "latex"), exist_ok=True)

    with open(os.path.join(all_eval_output_tables_dir, "latex", "concise_project_aggr_results.tex"), "w") as text_file:
        text_file.write(latex_table)

if __name__ == '__main__':
    if len(sys.argv) != 6:
        print("Usage: python evaluation.py <cmender_output_dir> <tupatcher_output_dir> <dataset_projects_info_path> <output_dir> <includes_path>")
        sys.exit(1)

    cmender_output_dir = sys.argv[1]
    tupatcher_output_dir = sys.argv[2]
    dataset_projects_info_filepath = sys.argv[3]
    output_dir = sys.argv[4]
    includes_path = os.path.abspath(sys.argv[5])

    print("cmender_output_dir: ", cmender_output_dir)
    print("tupatcher_output_dir: ", tupatcher_output_dir)
    print("dataset_projects_info_filepath: ", dataset_projects_info_filepath)
    print("output_dir: ", output_dir)

    dataset_projects_info = json.load(open(dataset_projects_info_filepath, "r"))

    evaluate(cmender_output_dir=cmender_output_dir, tupatcher_output_dir=tupatcher_output_dir,
              eval_output_dir=output_dir, dataset_projects_info=dataset_projects_info, includes_path=includes_path)
