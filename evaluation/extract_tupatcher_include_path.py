import os
import shutil

if __name__ == '__main__':
    # args
    
    # if len(sys.argv) != 3:
    #    print("Usage: python evaluation.py <cmender_output_dir> <output_dir>")
    # cmender_output_dir = sys.argv[1]
    # output_dir = sys.argv[2]

    cmender_output_dir = "../results/cmender_outputs_headers"
    output_dir = "../results/tupatcher_include_paths"

    os.makedirs(output_dir, exist_ok=True)

    for name in os.listdir(cmender_output_dir):
        project_path = os.path.join(cmender_output_dir, name)

        if os.path.isfile(project_path):
            continue

        print("Processing project: " + name)

        for name in os.listdir(project_path):
            subdirpath = os.path.join(project_path, name)

            if os.path.isfile(subdirpath):
                continue

            print("Processing subdirectory: " + name)

            include_path_dir = os.path.join(subdirpath, "includes")

            print("Include path: " + include_path_dir)

            for name in os.listdir(include_path_dir):
                if name == "cmender_mends.h":
                    continue

                print("Processing file: " + name)


                include_output_dir = os.path.join(output_dir, name)

                if os.path.isdir(os.path.join(include_path_dir, name)):
                    shutil.copytree(os.path.join(include_path_dir, name), include_output_dir, dirs_exist_ok=True)
                else:
                    if not os.path.exists(include_output_dir):
                        shutil.copyfile(os.path.join(include_path_dir, name), include_output_dir)
                    #shutil.copy2(os.path.join(include_path_path, name), os.path.join(output_dir, name))



                



