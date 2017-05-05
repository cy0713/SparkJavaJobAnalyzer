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


URL_CRYSTAL_API = 'http://10.30.230.217:8000/'
AUTH_URL='http://10.30.230.217:5000/v2.0'
USERNAME='admin'
PASSWORD='admin'
TENANT='crystaltest'

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
    policy_id = "366756dbfd024e0aa7f204a7498dfcfa:data1:28"

    url = URL_CRYSTAL_API + "controller/static_policy/" + str(policy_id)

    headers["X-Auth-Token"] = str(token)
    headers['Content-Type'] = "application/json"
    
    lambdas_as_string = ''
    index = 1
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
            
        
'''STEP 1: Execute the JobAnalyzer'''
jsonObject = executeJavaAnalyzer('/home/user/Desktop/SparkJavaJobAnalyzer.jar', 
                                 '/home/user/Desktop/SparkJavaSimpleTextAnalysis.java')

'''STEP 2: Get the lambdas and the code of the Job'''
lambdasToMigrate = jsonObject.get("lambdas")
originalJobCode = jsonObject.get("original-job-code")
pushdownJobCode = jsonObject.get("pushdown-job-code")

'''STEP 3: Decide whether or not to execute the lambda pushdown'''
'''TODO: This will be the second phase'''
pushdown = True
jobToCompile = originalJobCode

if pushdown:
    '''STEP 4: Set the lambdas in the storlet'''
    update_filter_params(lambdasToMigrate)
    jobToCompile = pushdownJobCode

'''STEP 5: Compile pushdown job'''
jobToCompile = jobToCompile.replace('package test.resources.test_jobs;',
                                    'package home.user.Desktop;')

jobFile = open('/home/user/Desktop/SparkJobMigratory.java', 'w')
print >> jobFile, jobToCompile.replace("SparkJavaSimpleTextAnalysisJava8Translated", "SparkJobMigratory")
cmd = '/usr/bin/javac ' + '-cp /home/user/Desktop/spark-core_2.11-2.1.0.jar:/home/user/Desktop/scala-library-2.12.2.jar '
cmd += '/home/user/Desktop/SparkJobMigratory.java' 
proc = subprocess.Popen(cmd, shell=True)
jobFile.close()

time.sleep(1)
'''STEP 6: Package the Spark Job class as a JAR and set the manifest'''
cmd = 'jar cfe /home/user/Desktop/SparkJobMigratory.jar home.user.Desktop.SparkJobMigratory /home/user/Desktop/SparkJobMigratory.class'
proc = subprocess.Popen(cmd, shell=True)
    
'''STEP 7: Execute the job against Swift'''

cmd = 'bash /home/user/workspace/spark-2.1.0-bin-hadoop2.7/bin/spark-submit /home/user/Desktop/SparkJobMigratory.jar --jars /home/user/workspace/spark-2.1.0-bin-hadoop2.7/jars/stocator-1.0.9.jar'
proc = subprocess.Popen(cmd, shell=True)