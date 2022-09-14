import os

test_dir_path = "../testcase/"
testdirs = ["2022_C"]
execute_script = "token_execute.sh"

for testdir in testdirs:
    for testfile in os.listdir(test_dir_path + testdir):
        testpath = os.path.join(test_dir_path, testdir, testfile)
        if (testfile[0] == 'i'):
            cmd = "cp " + testpath + " input.txt"
            os.system(cmd)
        if (testfile[0] == 't'):  
            cmd = "cp " + testpath + " testfile.txt"   
            os.system(cmd)
            test_name = testdir + "_" + testfile[0:-4]
            print("Now walking " + test_name)
            os.system("./" + execute_script + " " + test_name)
        if os.path.exists("./testfile.txt"):
            os.remove("./testfile.txt")
        if os.path.exists("./input.txt"):
            os.remove("./input.txt")
        if os.path.exists("./output.txt"):
            os.remove("./output.txt")
        if os.path.exists("./jre.txt"):
            os.remove("./jre.txt")
