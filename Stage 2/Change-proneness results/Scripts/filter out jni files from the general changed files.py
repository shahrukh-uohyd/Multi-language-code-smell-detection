#filter out jni files from the general changed files
import os
import pandas as pd
import csv

# Modifying the jni file names because it contains the repo name and project name also which is not there in smelly and faulty files
def transform_jni_path(path, project_name):
    path = str(path).strip()
    return path.replace("cloned_abidi/", "").replace(f"{project_name}/","")


# Directories

changed_dir = "buggy_smelly/abidi/general_faulty" #path to the general changes files
jni_dir = "buggy_smelly/abidi/JNIfiles_javaImpl"  # <-- JNI files by project
output_dir = "buggy_smelly/abidi/FaultyJNI"  #path to output

# Get  file lists
changed_files = sorted(os.listdir(changed_dir))
jni_files = sorted(os.listdir(jni_dir))

print(len(changed_files))
print(len(jni_files))
for changed_file, jni_file in zip(changed_files, jni_files):
        project_name = changed_file.replace(".csv", "")

        changed_path = os.path.join(changed_dir, changed_file)
        jni_path = os.path.join(jni_dir, changed_file)

        # Read CSVs
        changed_df = pd.read_csv(changed_path)
        jni_df = pd.read_csv(jni_path)

        # Get sets of file names (first column assumed)
        changed_set = set(changed_df.iloc[:, 0].dropna())
        jni_set = set(transform_jni_path(p, project_name) for p in jni_df.iloc[:, 0].dropna())


        # Filter only those in the JNI list
        changed_jni = changed_set & jni_set
        
        
        with open(os.path.join(output_dir, f"{project_name}.csv"), 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["File Path"])
            for fpath in sorted(changed_jni):
                writer.writerow([fpath])
