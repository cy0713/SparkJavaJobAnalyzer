'''
Created on Mar 13, 2017

@author: Raul Gracia
'''

from subprocess import PIPE, STDOUT, Popen
import json
from StringIO import StringIO
import requests
import keystoneclient.v2_0.client as keystone_client
import subprocess
import time
import sys
import os
import re


URL_CRYSTAL_API = 'http://10.30.230.217:8000/'
AUTH_URL='http://10.30.230.217:5000/v2.0'
USERNAME='admin'
PASSWORD='admin'
TENANT='crystaltest'
EXECUTOR_LOCATION = '/home/user/Desktop/'
JAVAC_PATH = '/usr/bin/javac'
SPARK_FOLDER = '/home/user/workspace/spark-2.1.0-bin-hadoop2.7/'
SPARK_LIBS_LOCATION = SPARK_FOLDER + '/jars/'

valid_token = None

def executeJavaAnalyzer(pathToJAR, pathToJobFile):
    
    p = Popen(['java', '-jar', pathToJAR, pathToJobFile], stdout=PIPE, stderr=STDOUT)    
    jsonResult = ''
        
    json_output = False
    for line in p.stdout:
        print line
        if line.startswith("{\"original-job-code\":"): json_output = True
        if (json_output): jsonResult += line
    
    print jsonResult
    io = StringIO(jsonResult)
    return json.load(io)

def update_filter_params(lambdasToMigrate):
    token = get_or_update_token()
    headers = {}
    
    #TODO: How to get the appropriate policy id to modify?
    policy_id = "366756dbfd024e0aa7f204a7498dfcfa:data1:31"

    url = URL_CRYSTAL_API + "controller/static_policy/" + str(policy_id)

    headers["X-Auth-Token"] = str(token)
    headers['Content-Type'] = "application/json"
    
    lambdas_as_string = ''
    index = 0
    for x in lambdasToMigrate:
        lambdas_as_string+= str(index) + "-lambda=" + str(x['lambda-type-and-body']) + ","
        index+=1

    print lambdas_as_string
    r = requests.put(str(url), json.dumps({'params': lambdas_as_string[:-1]}), headers=headers)
    print r.status_code
    
    
def get_keystone_admin_auth():
    admin_project = TENANT
    admin_user = USERNAME
    admin_passwd = PASSWORD
    keystone_url = AUTH_URL

    keystone = None
    try:
        keystone = keystone_client.Client(auth_url=keystone_url,
                                          username=admin_user,
                                          password=admin_passwd,
                                          tenant_name=admin_project)
    except Exception as exc:
        print(exc)

    return keystone

def get_or_update_token():
    global valid_token
    
    if valid_token == None:
        keystone = get_keystone_admin_auth()
        valid_token = keystone.auth_token
        print "AUTH TOKEN: ", valid_token
        
    return valid_token  
      
      
def main(argv=None):
    
    if argv is None:
        argv = sys.argv 
        
    print argv
    '''STEP 1: Execute the JobAnalyzer'''
    job_analyzer = argv[1]
    spark_job_path = argv[2]
    spark_job_name = spark_job_path[spark_job_path.rfind('/')+1:spark_job_path.rfind('.')]
    jsonObject = executeJavaAnalyzer(job_analyzer, spark_job_path)
    
    '''STEP 2: Get the lambdas and the code of the Job'''
    lambdasToMigrate = jsonObject.get("lambdas")
    originalJobCode = jsonObject.get("original-job-code")
    pushdownJobCode = jsonObject.get("pushdown-job-code")
    
    '''STEP 3: Decide whether or not to execute the lambda pushdown'''
    '''TODO: This will be the second phase'''
    pushdown = False
    jobToCompile = originalJobCode
    
    '''STEP 4: Set the lambdas in the storlet if necessary'''
    if pushdown:
        update_filter_params(lambdasToMigrate)
        jobToCompile = pushdownJobCode
    else: update_filter_params([])
    
    '''STEP 5: Compile pushdown/original job'''
    m = re.search('package\s*(\w\.?)*\s*;', jobToCompile)
    jobToCompile = jobToCompile.replace(m.group(0), 
                'package ' + EXECUTOR_LOCATION.replace('/','.')[1:-1] + ';')    
    jobToCompile = jobToCompile.replace(spark_job_name, "SparkJobMigratory")
    
    jobFile = open(EXECUTOR_LOCATION + '/SparkJobMigratory.java', 'w')
    print >> jobFile, jobToCompile
    jobFile.close() 
    
    print "Starting compilation"
    cmd = JAVAC_PATH + ' -cp \"'+ SPARK_LIBS_LOCATION + '*\" '
    cmd += EXECUTOR_LOCATION + 'SparkJobMigratory.java' 
    proc = subprocess.Popen(cmd, shell=True)
    print cmd
       
    
    '''STEP 6: Package the Spark Job class as a JAR and set the manifest'''
    print "Starting packaging"
    time.sleep(1)
    cmd = 'jar -cfe ' + EXECUTOR_LOCATION + 'SparkJobMigratory.jar ' + \
                       EXECUTOR_LOCATION.replace('/','.')[1:] + 'SparkJobMigratory ' + \
                       EXECUTOR_LOCATION + 'SparkJobMigratory.class'
    print cmd
    proc = subprocess.Popen(cmd, shell=True)
        
    print "Starting execution"
    '''STEP 7: Execute the job against Swift'''
    cmd = 'bash ' + SPARK_FOLDER+ 'bin/spark-submit ' + \
            EXECUTOR_LOCATION + 'SparkJobMigratory.jar --jars ' \
                + SPARK_FOLDER + 'jars/*.jar'
    proc = subprocess.Popen(cmd, shell=True)
    
    '''STEP 8: Clean files'''
    #time.sleep(1)
    #os.remove(EXECUTOR_LOCATION + 'SparkJobMigratory.java')
    #os.remove(EXECUTOR_LOCATION + 'SparkJobMigratory.class')
    #os.remove(EXECUTOR_LOCATION + spark_job_name + 'Java8Translated.java')
    
    
if __name__ == "__main__":
    sys.exit(main())     