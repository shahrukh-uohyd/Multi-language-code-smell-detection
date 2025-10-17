#filter out jni files from the general faulty files
import os
import pandas as pd
import csv

# Modifying the jni file names because it contains the repo name and project name also which is not there in smelly and faulty files
def transform_jni_path(path, project_name):
    path = str(path).strip()
    return path.replace("cloned_abidi/", "").replace(f"{project_name}/","")


# Directories
faulty_dir = "buggy_smelly/abidi/general_faulty" # path where the general faulty files are stored

jni_dir = "buggy_smelly/abidi/JNIfiles_javaImpl"  # <-- Path where the JNI files per projects are stored
output_dir = "buggy_smelly/abidi/FaultyJNI" # path to store the results

# Get file lists
faulty_files = sorted(os.listdir(faulty_dir))
jni_files = sorted(os.listdir(jni_dir))

for faulty_file, jni_file in zip(faulty_files, jni_files):
        project_name = faulty_file.replace(".csv", "")

        faulty_path = os.path.join(faulty_dir, faulty_file)
        
        jni_path = os.path.join(jni_dir, faulty_file)

        # Read CSVs
        faulty_df = pd.read_csv(faulty_path)
        
        jni_df = pd.read_csv(jni_path)

        # Get sets of file names (first column assumed)
        faulty_set = set(faulty_df.iloc[:, 0].dropna())
        
        jni_set = set(transform_jni_path(p, project_name) for p in jni_df.iloc[:, 0].dropna())


        # Filter only those in the JNI list
        faulty_jni = faulty_set & jni_set
        #buggy_jni = buggy_set & jni_set

  
        
        
        with open(os.path.join(output_dir, f"{project_name}.csv"), 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(["File Path"])
            for fpath in sorted(faulty_jni):
                writer.writerow([fpath])
