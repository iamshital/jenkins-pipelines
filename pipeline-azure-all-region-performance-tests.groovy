//Shital Savekar (v-shisav@microsoft.com)
//Status - BETA.

def azureRegions = azureRegions
def Network_SRIOV = Network_SRIOV
def Network_Synthetic = Network_Synthetic
def Storage_4K = Storage_4K
def Storage_1024K = Storage_1024K

//Details will added to Jenkins credentials later, once we have pipeline ready and stable.
def UbuntuARMImage="Canonical UbuntuServer 16.04-LTS latest"
def ShortDistroName="PPU16"   

def RunPowershellCommand(psCmd) {
    bat "powershell.exe -NonInteractive -ExecutionPolicy Bypass -Command \"[Console]::OutputEncoding=[System.Text.Encoding]::UTF8;$psCmd;EXIT \$global:LastExitCode\""
}


println "check 1: ${azureRegions}"
testRegions = azureRegions.split(',')
stage ('SRIOV Network Tests')
{
    if ( "${Network_SRIOV}" == "true" )
    {
        def NetworkTestSuites_SRIOV = [:]
        int i = 0
        testRegions.each 
        {
            NetworkTestSuites_SRIOV["${it}"] = 
            {
                try
                {
                    stage ("${it}") 
                    {
                        println "Running ${it}..."
                        node('slave-azure-1') 
                        {
                            withCredentials([file(credentialsId: 'Azure_Secrets', variable: 'Azure_Secrets_File')]) 
                            {						
                                git "https://github.com/iamshital/azure-linux-automation.git"
                                println "'${it}'"
                                RunPowershellCommand(".\\RunAzureTests.ps1" + 
                                " -testLocation '${it}'" +
                                " -DistroIdentifier '${ShortDistroName}-NTSR'" +
                                " -testCycle 'PERF-NTTTCP'" +
                                " -OverrideVMSize 'Standard_D15_v2'" +
                                " -ARMImageName '${UbuntuARMImage}'" +
                                " -StorageAccount 'ExistingStorage_Standard'" +
                                " -ResultDBTable 'Perf_Network_TCP_Azure_DefaultKernel'" +
                                " -ResultDBTestTag 'NTTTCP-TEST'" +
                                " -EnableAcceleratedNetworking" +
                                " -ForceDeleteResources "
                                )
                            }
                        }
                    }
                    
                }
                catch (exc)
                {
                    currentBuild.result = 'FAILURE'
                    println "${it}: STAGE_FAILED_EXCEPTION."
                }
                finally
                {
                    //println "${it}: ${currentBuild.result}"
                }
            }
        }
        parallel NetworkTestSuites_SRIOV
    }
    else
    {
        println "You did not ran Network SRIOV Tests"
    }
}

stage ('Synthetic Network Tests')
{
    if ( "${Network_Synthetic}" == "true" )
    {
        def NetworkTestSuites_Synthetic = [:]
        int i = 0
        testRegions.each 
        {
            NetworkTestSuites_Synthetic["${it}"] = 
            {
                try
                {
                    stage ("${it}") 
                    {
                        println "Running ${it}..."
                        node('slave-azure-1') 
                        {	
                            withCredentials([file(credentialsId: 'Azure_Secrets', variable: 'Azure_Secrets_File')]) 
                            {                            		
                                git "https://github.com/iamshital/azure-linux-automation.git"
                                RunPowershellCommand(".\\RunAzureTests.ps1" + 
                                " -testLocation '${it}'" +
                                " -DistroIdentifier '${ShortDistroName}-NTSN'" +
                                " -testCycle 'PERF-NTTTCP'" +
                                " -OverrideVMSize 'Standard_D15_v2'" +
                                " -ARMImageName '${UbuntuARMImage}'" +
                                " -StorageAccount 'ExistingStorage_Standard'" +
                                " -ResultDBTable 'Perf_Network_TCP_Azure_DefaultKernel'" +
                                " -ResultDBTestTag 'NTTTCP-TEST'" +
                                " -ForceDeleteResources "
                                )
                            }
                        }
                    }
                    
                }
                catch (exc)
                {
                    currentBuild.result = 'FAILURE'
                    println "${it}: STAGE_FAILED_EXCEPTION."
                }
                finally
                {
                    //println "${it}: ${currentBuild.result}"
                }
            }
        }
        parallel NetworkTestSuites_Synthetic
    }
    else
    {
        println "You did not ran Network Synthetic Tests"
    }
}

stage ('FIO 4K')
{
    if ( "${Storage_4K}" == "true" )
    {
        def FIO_4K = [:]
        int i = 0
        testRegions.each 
        {
            FIO_4K["${it}"] = 
            {
                try
                {
                    stage ("${it}") 
                    {
                        println "Running ${it}..."
                        node('slave-azure-1') 
                        {		
                            withCredentials([file(credentialsId: 'Azure_Secrets', variable: 'Azure_Secrets_File')]) 
                            {                            	
                                git "https://github.com/iamshital/azure-linux-automation.git"
                                RunPowershellCommand(".\\RunAzureTests.ps1" + 
                                " -testLocation '${it}'" +
                                " -DistroIdentifier '${ShortDistroName}-FIO4K'" +
                                " -testCycle 'PERF-FIO'" +
                                " -OverrideVMSize 'Standard_DS14_v2'" +
                                " -ARMImageName '${UbuntuARMImage}'" +
                                " -RunSelectedTests 'ICA-PERF-FIO-TEST-4K'" +
                                " -StorageAccount 'NewStorage_Premium'" +
                                " -ResultDBTable 'Perf_Storage_Azure_DefaultKernel'" +
                                " -ResultDBTestTag 'UBUNTU1604-FIO-4K'" +
                                " -ForceDeleteResources "							
                                )
                            }                            
                        }
                    }
                }
                catch (exc)
                {
                    currentBuild.result = 'FAILURE'
                    println "${it}: STAGE_FAILED_EXCEPTION."
                }
                finally
                {
                    //println "${it}: ${currentBuild.result}"
                }
            }
        }
        parallel FIO_4K
    }
    else
    {
        println "You did not ran 4K storage Tests"
    }    
}

stage ('FIO 1024K')
{
    if ( "${Storage_1024K}" == "true" )
    {
        def FIO_1024K = [:]
        int i = 0
        testRegions.each 
        {
            FIO_1024K["${it}"] = 
            {
                try
                {
                    stage ("${it}") 
                    {
                        println "Running ${it}..."
                        node('slave-azure-1') 
                        {	
                            withCredentials([file(credentialsId: 'Azure_Secrets', variable: 'Azure_Secrets_File')]) 
                            {                            		
                                git "https://github.com/iamshital/azure-linux-automation.git"
                                RunPowershellCommand(".\\RunAzureTests.ps1" + 
                                " -testLocation '${it}'" +
                                " -DistroIdentifier '${ShortDistroName}-FIO1M'" +
                                " -testCycle 'PERF-FIO'" +
                                " -OverrideVMSize 'Standard_DS14_v2'" +
                                " -ARMImageName '${UbuntuARMImage}'" +
                                " -RunSelectedTests 'ICA-PERF-FIO-TEST-1024K'" +
                                " -StorageAccount 'NewStorage_Premium'" +
                                " -ResultDBTable 'Perf_Storage_Azure_DefaultKernel'" +
                                " -ResultDBTestTag 'UBUNTU1604-FIO-1024K'" +
                                " -ForceDeleteResources "							
                                )
                            }                            
                        }
                    }
                    
                }
                catch (exc)
                {
                    currentBuild.result = 'FAILURE'
                    println "${it}: STAGE_FAILED_EXCEPTION."
                }
                finally
                {
                    //println "${it}: ${currentBuild.result}"
                }
            }
        }
        parallel FIO_1024K
    }
    else
    {
        println "You did not ran 1024K storage Tests"
    }    
}