//Shital Savekar (v-shisav@microsoft.com)
//Status - BETA.

def LAGSCOPE_Synthetic = LAGSCOPE_Synthetic
def NTTTCP_Synthetic = NTTTCP_Synthetic
def LAGSCOPE_SRIOV = LAGSCOPE_SRIOV
def NTTTCP_SRIOV = NTTTCP_SRIOV
def IPERF_SINGLE_CONN_SRIOV = IPERF_SINGLE_CONN_SRIOV
def FIO_12Disks = FIO_12Disks
def vmSizes = vmSizes

//Details will added to Jenkins credentials later, once we have pipeline ready and stable.
def UbuntuARMImage="Canonical UbuntuServer 16.04-LTS latest"
def ShortDistroName="PPU16"   
def CurrentTestName

def RunPowershellCommand(psCmd) {
    bat "powershell.exe -NonInteractive -ExecutionPolicy Bypass -Command \"[Console]::OutputEncoding=[System.Text.Encoding]::UTF8;$psCmd;EXIT \$global:LastExitCode\""
}



stage ('SRIOV Network Tests')
{
	def NetworkTestSuites_SRIOV = [:]
	int i = 0
	CurrentTestName = "NTTTCP SRIOV"
	if ( "${NTTTCP_SRIOV}" == 'true' ) 
	{
		NetworkTestSuites_SRIOV["${CurrentTestName}"] = 
		{
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{			
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'westus2'" +
							" -DistroIdentifier '${ShortDistroName}-NTDKSR'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}			
	
	CurrentTestName = "LAGSCOPE SRIOV"
	if ( "${LAGSCOPE_SRIOV}" == 'true' ) 
	{
		
		NetworkTestSuites_SRIOV["${CurrentTestName}"] = 
		{                      
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'westus'" +
							" -DistroIdentifier '${ShortDistroName}-LGDKSR'" +
							" -testCycle 'PERF-LAGSCOPE'" +
							" -OverrideVMSize 'Standard_D15_v2'" +
							" -ARMImageName '${UbuntuARMImage}'" +
							" -StorageAccount 'ExistingStorage_Standard'" +
							" -ResultDBTable 'Perf_Network_Latency_Azure_DefaultKernel'" +
							" -ResultDBTestTag 'LAGSCOPE-TEST'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}                
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}
	
	CurrentTestName = "Iperf3 Single Connection SRIOV"
	if ( "${IPERF_SINGLE_CONN_SRIOV}" == 'true' ) 
	{
		
		NetworkTestSuites_SRIOV["${CurrentTestName}"] = 
		{                      
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'westus'" +
							" -DistroIdentifier '${ShortDistroName}-LGDKSR'" +
							" -testCycle 'PERF-IPERF3-SINGLE-CONNECTION'" +
							" -OverrideVMSize 'Standard_D15_v2'" +
							" -ARMImageName '${UbuntuARMImage}'" +
							" -StorageAccount 'ExistingStorage_Standard'" +
							" -ResultDBTable 'Perf_Network_Single_TCP_Azure_DefaultKernel'" +
							" -ResultDBTestTag 'LAGSCOPE-TEST'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}                
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}	
	if ( ( "${LAGSCOPE_SRIOV}" == 'false' ) && ( "${NTTTCP_SRIOV}" == 'false' ) && ( "${IPERF_SINGLE_CONN_SRIOV}" == 'false' ) )
	{
		println "You did not ran any SRIOV Network tests."
	}
	else
	{
		parallel NetworkTestSuites_SRIOV
	}
}

stage ('Synthetic Network Tests')
{
	NetworkTestSuites_Synthetic = [:]
	int i = 0
	//Stage BVT
	
	CurrentTestName = "NTTTCP Synthetic"
	if ( "${NTTTCP_Synthetic}" == 'true' ) 
	{
		NetworkTestSuites_Synthetic["${CurrentTestName}"] = 
		{
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'westus'" +
							" -DistroIdentifier '${ShortDistroName}-NTDKSN'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}	
	
	CurrentTestName = "LAGSCOPE Synthetic"
	if ( "${LAGSCOPE_Synthetic}" == 'true' ) 
	{
		NetworkTestSuites_Synthetic["${CurrentTestName}"] = 
		{                      
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{	
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'westus'" +
							" -DistroIdentifier '${ShortDistroName}-LGDKSR'" +
							" -testCycle 'PERF-LAGSCOPE'" +
							" -OverrideVMSize 'Standard_D15_v2'" +
							" -ARMImageName '${UbuntuARMImage}'" +
							" -StorageAccount 'ExistingStorage_Standard'" +
							" -ResultDBTable 'Perf_Network_Latency_Azure_DefaultKernel'" +
							" -ResultDBTestTag 'LAGSCOPE-TEST'" +
							" -ForceDeleteResources "
							)
						}
					}
				}
			}
			catch (exc)
			{
				currentBuild.result = 'FAILURE'
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}                
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}	
	if ( ( "${LAGSCOPE_Synthetic}" == 'false' ) && ( "${NTTTCP_Synthetic}" == 'false' ) )
	{
		println "You did not ran any Synthetic Network tests."
	}
	else
	{
		parallel NetworkTestSuites_Synthetic
	}
}
stage ('Storage Performance')
{
	Storage_Premium = [:]
	int i = 0
	
	CurrentTestName = "FIO 4K I/0"
	if ( "${FIO_12Disks}" == 'true' ) 
	{
		
		Storage_Premium["${CurrentTestName}"] = 
		{
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'centralus'" +
							" -DistroIdentifier '${ShortDistroName}-FIOLT4K'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}	
	CurrentTestName = "FIO 1024K I/0"
	if ( "${FIO_12Disks}" == 'true' ) 
	{
		
		Storage_Premium["${CurrentTestName}"] = 
		{
			try
			{
				stage ("${CurrentTestName}") 
				{
					println "Running ${CurrentTestName}..."
					node('ostcjenkins-azure') 
					{
						withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
						{						
							git "https://github.com/iamshital/azure-linux-automation.git"
							RunPowershellCommand(".\\RunAzureTests.ps1" + 
							" -testLocation 'centralus'" +
							" -DistroIdentifier '${ShortDistroName}-FIOLT1M'" +
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
				println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
			}
			finally
			{
				//println "${CurrentTestName}: ${currentBuild.result}"
			}
		}
	}
	else
	{
		println "You skipped ${CurrentTestName} tests."
	}	
		
	if ( ( "${FIO_12Disks}" == 'false' ) && ( "${FIO_12Disks}" == 'false' ) )
	{
		println "You did not ran any Storage tests."
	}
	else
	{
		parallel Storage_Premium
	}
}


def allvmSizes = vmSizes.split(',')
def currentVMsize
allvmSizes.each 
{
	currentVMsize = "${it}"
	if ( "${currentVMsize}" == "Standard_D64_v3" )
	{
		stage ("${currentVMsize}")
		{

			Standard_D64_v3 = [:]
			CurrentTestName = "NTTTCP Synthetic"
			Standard_D64_v3["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus'" +
								" -DistroIdentifier '${ShortDistroName}-D64NTDKSN'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}
			CurrentTestName = "NTTTCP SRIOV"
			Standard_D64_v3["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus2'" +
								" -DistroIdentifier '${ShortDistroName}-D64NTDKSR'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}			
			parallel Standard_D64_v3
		}
	}
	if ( "${currentVMsize}" == "Standard_E64_v3" )
	{
		stage ("${currentVMsize}")
		{

			Standard_E64_v3 = [:]
			CurrentTestName = "NTTTCP Synthetic"
			Standard_E64_v3["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus'" +
								" -DistroIdentifier '${ShortDistroName}-E64NTDKSN'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}
			CurrentTestName = "NTTTCP SRIOV"
			Standard_E64_v3["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus2'" +
								" -DistroIdentifier '${ShortDistroName}-E64NTDKSR'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}			
			parallel Standard_E64_v3
		}
	}
	if ( "${currentVMsize}" == "Standard_M64ms" )
	{
		stage ("${currentVMsize}")
		{

			Standard_M64ms = [:]
			CurrentTestName = "NTTTCP Synthetic"
			Standard_M64ms["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus'" +
								" -DistroIdentifier '${ShortDistroName}-M64NTDKSN'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}
			CurrentTestName = "NTTTCP SRIOV"
			Standard_M64ms["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus2'" +
								" -DistroIdentifier '${ShortDistroName}-M64NTDKSR'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}			
			parallel Standard_M64ms
		}
	}
	if ( "${currentVMsize}" == "Standard_G5" )
	{
		stage ("${currentVMsize}")
		{

			Standard_G5 = [:]
			CurrentTestName = "NTTTCP Synthetic"
			Standard_G5["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus'" +
								" -DistroIdentifier '${ShortDistroName}-G5NTDKSN'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}
			CurrentTestName = "NTTTCP SRIOV"
			Standard_G5["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "SRIOV is not supported for : ${CurrentTestName}!"
						node('ostcjenkins-azure') 
						{
							//withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							//{						
							//	git "https://github.com/iamshital/azure-linux-automation.git"
							//	RunPowershellCommand(".\\RunAzureTests.ps1" + 
							//	" -testLocation 'westus2'" +
							//	" -DistroIdentifier '${ShortDistroName}-G5NTDKSN'" +
							//	" -testCycle 'PERF-NTTTCP'" +
							//	" -OverrideVMSize '${currentVMsize}'" +
							//	" -ARMImageName '${UbuntuARMImage}'" +
							//	" -StorageAccount 'ExistingStorage_Standard'" +
							//	" -ResultDBTable 'Perf_Network_TCP_Azure_DefaultKernel'" +
							//	" -ResultDBTestTag 'NTTTCP-TEST'" +
							//	" -EnableAcceleratedNetworking" +
							//	" -ForceDeleteResources "							
							//	)
							//}
						}
					}
				}
				catch (exc)
				{
					currentBuild.result = 'FAILURE'
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}			
			parallel Standard_G5
		}
	}
	if ( "${currentVMsize}" == "Standard_M128s" )
	{
		stage ("${currentVMsize}")
		{

			Standard_M128s = [:]
			CurrentTestName = "NTTTCP Synthetic"
			Standard_M128s["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus'" +
								" -DistroIdentifier '${ShortDistroName}-M128NTDKSN'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}
			CurrentTestName = "NTTTCP SRIOV"
			Standard_M128s["${CurrentTestName}"] = 
			{
				try
				{
					stage ("${CurrentTestName}") 
					{
						println "Running ${CurrentTestName}..."
						node('ostcjenkins-azure') 
						{
							withCredentials([file(credentialsId: 'Azure_Secrets_File', variable: 'Azure_Secrets_File')]) 
							{						
								git "https://github.com/iamshital/azure-linux-automation.git"
								RunPowershellCommand(".\\RunAzureTests.ps1" + 
								" -testLocation 'westus2'" +
								" -DistroIdentifier '${ShortDistroName}-M128NTDKSR'" +
								" -testCycle 'PERF-NTTTCP'" +
								" -OverrideVMSize '${currentVMsize}'" +
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
					println "${CurrentTestName}: STAGE_FAILED_EXCEPTION."
				}
				finally
				{
					//println "${CurrentTestName}: ${currentBuild.result}"
				}
			}			
			parallel Standard_M128s
		}
	}	
}
