import os
import sys
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

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

def bar_chart_util(df, column, title=None, xlabel=None, save_path=None, figname=None):
    counts = df[column].value_counts()
    percentages = counts / counts.sum() * 100

    percentages_df = pd.DataFrame({column: percentages.index, "percentage": percentages.values})

    plt.figure(figsize=(10, 6))

    sns.barplot(data=percentages_df, x="percentage", y=column, color="skyblue", edgecolor="blue")

    if title:
        plt.title(title)

    if xlabel:
        plt.xlabel(xlabel)

    plt.ylabel("")

    if save_path:
        plt.savefig(os.path.join(save_path, figname))
        plt.close()
    else:
        plt.show()

class RepoDetailsDesc:
    def __init__(self, repos_df, save_path=None):
        self.repos_df = repos_df
        self.save_path = save_path
        self.repos_df["license_spdx_id"] = self.repos_df["license_spdx_id"].replace("NOASSERTION", "Other")

    def forks_histogram(self):
        histogram_util(self.repos_df, "forks", title="Forks histogram", xlabel="Forks",
                    save_path=self.save_path, figname="forks_hist.pdf")

    def stargazers_histogram(self):
        histogram_util(self.repos_df, "stargazers", title="Stargazers histogram", xlabel="Stargazers",
                    save_path=self.save_path, figname="stargazers_hist.pdf")

    def c_ratio_histogram(self):
        histogram_util(self.repos_df, "c_ratio", bins=10, title="C ratio histogram", xlabel="C ratio",
                    save_path=self.save_path, figname="c_ratio_hist.pdf")

    def cpp_ratio_histogram(self):
        histogram_util(self.repos_df, "cpp_ratio", bins=10, title="C++ ratio histogram", xlabel="C++ ratio",
                    save_path=self.save_path, figname="cpp_ratio_hist.pdf")

    def c_cpp_ratio_histogram(self):
        histogram_util(self.repos_df, "c_cpp_ratio", bins=10, title="C/C++ ratio histogram", xlabel="C/C++ ratio",
                    save_path=self.save_path, figname="c_cpp_ratio_hist.pdf")

    def build_lang_ratio_histogram(self):
        histogram_util(self.repos_df, "build_lang_ratio", bins=10, title="Build language ratio histogram", xlabel="Build language",
                    save_path=self.save_path, figname="build_lang_ratio_hist.pdf")

    def makefile_histogram(self):
        histogram_util(self.repos_df, "makefile_ratio", bins=4, title="Makefile ratio histogram", xlabel="Makefile",
                    save_path=self.save_path, figname="makefile_ratio_hist.pdf")

    def cmake_histogram(self):
        histogram_util(self.repos_df, "cmake_ratio", bins=4, title="CMake ratio histogram", xlabel="CMake",
                    save_path=self.save_path, figname="cmake_ratio_hist.pdf")

    def starlark_histogram(self):
        histogram_util(self.repos_df, "starlark_ratio", bins=4, title="Starlark ratio histogram", xlabel="Starlark",
                    save_path=self.save_path, figname="starlark_ratio_hist.pdf")

    def meson_histogram(self):
        histogram_util(self.repos_df, "meson_ratio", bins=4, title="Meson ratio histogram", xlabel="Meson",
                    save_path=self.save_path, figname="meson_ratio_hist.pdf")

    def ninja_histogram(self):
        histogram_util(self.repos_df, "ninja_ratio", bins=4, title="Ninja ratio histogram", xlabel="Ninja",
                    save_path=self.save_path, figname="ninja_ratio_hist.pdf")

    def qmake_histogram(self):
        histogram_util(self.repos_df, "qmake_ratio", bins=4, title="QMake ratio histogram", xlabel="QMake",
                    save_path=self.save_path, figname="qmake_ratio_hist.pdf")

    def stargazers_vs_forks_scatter_plot(self):
        scatter_plot_util(self.repos_df, "forks", "stargazers", adjust_left=0.2, 
                          title="Stargazers vs Forks", xlabel="Forks", ylabel="Stargazers",
                        save_path=self.save_path, figname="stargazers_vs_forks_scatter.pdf")

    def c_vs_cpp_ratio_scatter_plot(self):
        scatter_plot_util(self.repos_df, "c_ratio", "cpp_ratio",  
                        title="C vs C++ ratio", xlabel="C ratio", ylabel="C++ ratio",
                        save_path=self.save_path, figname="c_vs_cpp_ratio_scatter.pdf")

    def c_cpp_ratio_vs_build_lang_ratio_scatter_plot(self):
        scatter_plot_util(self.repos_df, "c_cpp_ratio", "build_lang_ratio",  
                        title="C/C++ ratio vs Build language ratio", xlabel="C/C++ ratio", ylabel="Build language ratio",
                        save_path=self.save_path, figname="c_cpp_ratio_vs_build_lang_ratio_scatter.pdf")

    def licenses_bar_chart(self):
        bar_chart_util(self.repos_df, "license_spdx_id", title="Licenses", xlabel="Relative Frequency (%)",
                    save_path=self.save_path, figname="licenses_bar.pdf")

    def all(self):
        self.forks_histogram()
        self.stargazers_histogram()
        self.c_ratio_histogram()
        self.cpp_ratio_histogram()
        self.c_cpp_ratio_histogram()
        self.build_lang_ratio_histogram()
        self.makefile_histogram()
        self.cmake_histogram()
        self.starlark_histogram()
        self.meson_histogram()
        self.ninja_histogram()
        self.qmake_histogram()
        self.stargazers_vs_forks_scatter_plot()
        self.c_vs_cpp_ratio_scatter_plot()
        self.c_cpp_ratio_vs_build_lang_ratio_scatter_plot()
        self.licenses_bar_chart()

'''
# 'forks' stats
print("Forks stats:")
print(repos_df["forks"].describe())
print()


# 'stargazers' stats
print("Stargazers stats:")
print(repos_df["stargazers"].describe())

'''

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python repo_details_desc.py <repos_df_filepath> <output_dirpath>")
        sys.exit(1)

    repos_df_filepath = sys.argv[1]
    output_dirpath = sys.argv[2]

    repos_df = pd.read_csv(repos_df_filepath)

    figures_dirpath = os.path.join(output_dirpath, "figures")

    if not os.path.exists(figures_dirpath):
        os.mkdir(figures_dirpath)

    repo_details_desc = RepoDetailsDesc(repos_df, save_path=figures_dirpath)
    repo_details_desc.all()

# clear ; python3 ./src/repo_details_desc.py ./output/repos.csv ./output