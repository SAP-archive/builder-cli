package com.hybris.builder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import sun.misc.IOUtils;



public class CLIRunner
{
    private Properties configProperties = new Properties();
    private Properties appProperties = new Properties();
    private Properties depVersionProperties = new Properties();
    private String javaBin;
    private String mvnBin;
    private String userHome;
    private File configFile;
    private File depVersionPropertiesFile;
    private String configFilePath;
    private String version;
    private String name;
    private String errorLogName;
    private int updateInterval = 3;
    private String ansi_white = "\u001B[37m";
    private String ansi_reset = "\u001B[0m";
    private String ansi_red = "\u001B[31m";
    private String ansi_blue = "\u001B[34m";
    private String javaOpts;
    private String versionUrl = "https://raw.githubusercontent.com/SAP/builder-cli/master/version.txt";

    private boolean networkConnection = true;
    private boolean appendToLog = false;

    public static void main(String [ ] args)
    {
        new CLIRunner().initialize(args);
    }

    public void initialize(String[] args)
    {
        try
        {
            if(isWindows())
            {
                ansi_red = "";
                ansi_reset = "";
                ansi_white = "";
                ansi_blue = "";
            }

            checkPrerequisites();

            if(args.length > 0)
            {
                if("clearCache".equals(args[0]))
                {
                    clearCommandDependencies();
                    System.exit(0);
                }
                else if("displayCachedVersions".equals(args[0]))
                {
                    for(String prop : depVersionProperties.stringPropertyNames())
                    {
                        System.out.println(prop + ":\t" + depVersionProperties.getProperty(prop));
                    }
                    System.exit(0);
                }
                else if("updateInterval".equals(args[0]))
                {
                    try {
                        if(args.length > 1) {
                            updateInterval = Integer.parseInt(args[1]);
                            configProperties.setProperty("updateInterval", String.valueOf(updateInterval));
                            saveConfigFile();
                        }
                        else
                        {
                            System.out.println(updateInterval);
                        }
                        System.exit(0);

                    } catch (NumberFormatException e) {
                        System.out.println("ERROR:\tupdateInterval must be a valid number.");
                        System.exit(1);
                    }
                }
                String command = args[0];
                Properties cmdProps = loadCommand(command);
                if(cmdProps == null)
                {
                    showUsage(command, true);
                    System.exit(0);
                }

                if("template".equals(cmdProps.getProperty("type")))
                {
                    createTemplate(cmdProps, args);
                }
                else
                {
                    int exitCode = execute(cmdProps, args, downloadDependencies(command, cmdProps));
                    if(appendToLog)
                    {
                        System.out.println("\nERROR: Command failed. Check '" +
                                errorLogName + "' for more info");
                    }
                    System.exit(exitCode);
                }
            }
            else
            {
                showUsage(null, false);
                System.exit(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void clearCommandDependencies()
    {
        System.out.println("Clearing cache");

        File cmDir = new File(configFilePath, "common");
        System.out.println("Deleting " + cmDir.getAbsolutePath());
        deleteRecursively(cmDir);

        String commands = appProperties.getProperty("commands");
        if(commands != null && commands.trim().length() > 0)
        {
            Set<String> commandSet = new LinkedHashSet<String>(Arrays.asList(commands.split(",")));
            for(String cmd : commandSet)
            {
                File cmdDir = new File(configFilePath, cmd);
                System.out.println("Deleting " + cmdDir.getAbsolutePath());
                deleteRecursively(cmdDir);
            }
        }
        if(depVersionPropertiesFile.exists())
        {
            depVersionPropertiesFile.delete();
        }
    }

    private void deleteRecursively(File path)
    {
        if(path.isDirectory())
        {
            for(File dep : path.listFiles())
            {
                deleteRecursively(dep);
            }
        }
        path.delete();
    }

    private void createTemplate(Properties cmdProps, String[] args) throws IOException
    {
        String templateName = cmdProps.getProperty("cmd");
        Map<String, String> argsMap = new HashMap<String, String>();
        for(int i = 1; cmdProps.containsKey("arg" + i); i++)
        {
            if(i >= args.length)
            {
                showUsage(templateName, true, "Error: Wrong number of arguments.");
                System.exit(0);
            }
            argsMap.put("ARG" + i, args[i]);
        }

        Properties templateProps = new Properties();
        templateProps.load(getClass().getClassLoader().getResourceAsStream("templates/" + templateName + "/template.properties"));
        String rawdirs = templateProps.getProperty("dirs");
        String rawfiles = templateProps.getProperty("files");
        File targetDirectory = new File(args[1]); 
        targetDirectory.mkdirs();


        // create dirs
        if(rawdirs != null && !rawdirs.trim().isEmpty())
        {
            String[] dirs = rawdirs.split(",");
            for(String dirname : dirs)
            {
                File dir = new File(targetDirectory, dirname);

                if(dir.exists())
                {
                    System.out.println("Error: Directory already exists: " + dir.getAbsolutePath());
                    System.exit(1);
                }

                dir.mkdirs();
            }
        }

        // create files
        if(rawfiles != null && !rawfiles.trim().isEmpty())
        {
            String[] files = rawfiles.split(",");
            for(String filename : files)
            {
                File file = new File(targetDirectory, filename);

                if(file.exists())
                {
                    System.out.println("Error: File already exists: " + file.getAbsolutePath());
                    System.exit(0);
                }

                file.createNewFile();

                System.out.println("Creating file " + targetDirectory.getName() + "/" + filename);

                String template = getString(getClass().getClassLoader()
                        .getResourceAsStream("templates/" + templateName + "/" + filename));

                if(template != null)
                {
                    writeStringToFile(processTemplate(template, argsMap), file);
                }
            }
        }
    }


    protected String getString(InputStream inputStream) throws IOException
    {
        int ch;
        StringBuilder sb = new StringBuilder();
        while((ch = inputStream.read())!= -1)
        {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    protected void showUsage(String command, boolean error) throws IOException
    {
        showUsage(command, error, null);
    }

    protected void showUsage(String command, boolean error, String errorMsg) throws IOException
    {
        System.out.println(ansi_white + name + ansi_reset + ", version " + version +"\n"+"Copyright (c) 2009-2015 SAP SE or an SAP affiliate company\n");
        String readReleasedVersion = readReleasedVersion();
        if(!readReleasedVersion.equals(version) && networkConnection==true){
            System.out.println(ansi_blue + name + " is outdated. Please download the newest "+ readReleasedVersion +" version!!!" + ansi_reset);
        }

        if(error)
        {
            String msg = errorMsg == null ? "ERROR: Unknown command '" + command + "'" : errorMsg;
            System.out.println(ansi_red + msg + ansi_reset);
        }
        System.out.println("\nUsage: " + appProperties.getProperty("cmdName") + " command [arg ...]");
        String commands = appProperties.getProperty("commands");
        if(commands != null && commands.trim().length() > 0)
        {
            Set<String> commandSet = new LinkedHashSet<String>(Arrays.asList(commands.split(",")));
            System.out.println("\nAvailable commands:\n");
            String arg = "arg";
            for(String cmd : commandSet)
            {
                Properties cmdProps = loadCommand(cmd);
                if(cmdProps.getProperty("arg1")!=null){
                    System.out.println("\t" + ansi_white + cmd + ansi_reset + (cmd.length() < 8 ? "\t" : "")
                            + " \t\t\t" + cmdProps.getProperty("description"));
                    for(int i = 1;;i++){
                        if(cmdProps.getProperty(arg+i)==null){
                            System.out.print("\n");
                            break;
                        }
                        System.out.println("\t\t\t\t\t" + ansi_white + arg+i + ansi_reset + ": " + cmdProps.getProperty("arg"+i));
                    }
                }else {
                    System.out.println("\t" + ansi_white + cmd + ansi_reset + (cmd.length() < 8 ? "\t" : "")
                            + "\t\t\t" + cmdProps.getProperty("description") + "\n");
                }
            }
        }
        System.out.println("\t" + ansi_white + "displayCachedVersions" + ansi_reset + "\t\t" + "Print a list with versions of cached dependencies" + "\n");
        System.out.println("\t" + ansi_white + "clearCache" + ansi_reset + "\t\t\t" + "Delete cached dependencies" + "\n");
        System.out.flush();
    }


    protected String processTemplate(String template, Map<String, String> params)
    {
        String processed = template;
        for(Map.Entry<String, String> param : params.entrySet())
        {
            processed = processed.replaceAll("\\{\\{" + param.getKey() + "}}", param.getValue());
        }
        return processed;
    }

    private Map<String, String> downloadDependencies(String command, Properties cmdProps) throws IOException
    {
        Map<String, String> depsMap = new HashMap<String, String>();
        String dependencies = cmdProps.getProperty("dependencies");

        if(dependencies != null && dependencies.trim().length() > 0)
        {
            //System.out.println("Downloading dependencies " + dependencies);

            File tmpDir = new File(configFilePath, "tmp");
            tmpDir.mkdirs();

            File cmdDepDir = new File(configFilePath, command);
            cmdDepDir.mkdirs();

            InputStream pomStream = getClass().getResourceAsStream("/pom_template.xml");
            byte[] bytes = IOUtils.readFully(pomStream, -1, true);
            String pomTemplate = new String(bytes);

            for(String rawDep : dependencies.trim().split(","))
            {
                String rawDepFinal = rawDep;
                File depDir = cmdDepDir;
                boolean commonDep = false;
                if(rawDep.startsWith("common#")) {
                    commonDep = true;
                    File commonDepDir = new File(configFilePath, "common");
                    commonDepDir.mkdirs();
                    depDir = commonDepDir;
                    Properties commonDepProperties = new Properties();
                    commonDepProperties.load(getClass().getResourceAsStream("/commonDependencies.properties"));
                    String art = commonDepProperties.getProperty(rawDep.replace("common#", ""));
                    if(art == null) {
                        System.out.println("ERROR: Could not resolve dependency " + rawDep);
                    }
                    rawDepFinal = rawDep + "@" + art;
                }

                String[] strings = rawDepFinal.split("@");
                String depAlias = strings[0];
                String[] artifactInfo = strings[1].split(":");
                String groupId = artifactInfo[0];
                String artifactId = artifactInfo[1];
                String version = artifactInfo[2];
                String type = artifactInfo.length > 3 ? artifactInfo[3] : "jar";
                String classifier = artifactInfo.length > 4 ? artifactInfo[4] : "";


                Map<String, String> params = new HashMap<String, String>();
                params.put("groupId", groupId);
                params.put("artifactId", artifactId);
                params.put("version", version);
                params.put("type", type);
                params.put("classifier", classifier);

                String processedTemplate = processTemplate(pomTemplate, params);

                // write pom to temporary directory
                writeStringToFile(processedTemplate, new File(tmpDir, "pom.xml"));

                File cmdDepFile = new File(depDir, depAlias + "." + type);
                boolean needUpdate = !cmdDepFile.exists();
                if(cmdDepFile.exists() && updateInterval >= 0)
                {
                    long modTimestamp = cmdDepFile.lastModified();
                    long msSinceLast = new Date().getTime() - modTimestamp;
                    if(updateInterval == 0 || msSinceLast > TimeUnit.DAYS.toMillis(updateInterval))
                    {
                        cmdDepFile.delete();
                        needUpdate = true;
                    }
                    else
                    {
                        needUpdate = false;
                    }
                }

                if(needUpdate)
                {
                    System.out.print("Downloading " + artifactId + " ... ");

                    boolean printOutput = "true".equals(configProperties.getProperty("verbose"));

                    int exitValue = execute(Arrays.asList(mvnBin, "-U", "-DoutputDirectory=" + tmpDir.getAbsolutePath(),
                            "-DincludeArtifactIds=" + artifactId,
                            "dependency:copy-dependencies"), tmpDir, printOutput);

                    System.out.println(exitValue == 0 ? "Done." : "Failed.");

                    if(exitValue != 0)
                    {
                        System.out.println("Could not connect to the repository. Please make sure that you have the correct settings.xml in .m2 directory.");
                        System.exit(exitValue);
                    }
                }
                else
                {
                    System.out.println("Using cached dependencies (Call 'clearCache' command to download again)");
                }

                depsMap.put(depAlias, cmdDepFile.getAbsolutePath());

                // delete all files in dir
                for(File file : tmpDir.listFiles())
                {
                    if(file.isFile())
                    {
                        if(file.getName().startsWith(artifactId))
                        {
                            Files.copy(file.toPath(),
                                    cmdDepFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            depVersionProperties.setProperty((commonDep ? "common" : command + "/" + depAlias), file.getName());
                        }
                        file.delete();
                    }
                }
            }

            tmpDir.delete();

            //TODO: cleanup
        }
        FileOutputStream fos = new FileOutputStream(depVersionPropertiesFile);
        depVersionProperties.store(fos, "");
        fos.close();
        return depsMap;
    }

    private void saveConfigFile() {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            configProperties.store(fos, "");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void writeStringToFile(String content, File file) throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(content);
        writer.close();
    }


    protected Properties loadCommand(String command) throws IOException {
        String commandSpec = "/commands/" + command + ".properties";
        Properties commandProperties = new Properties();
        InputStream cmdStream = this.getClass().getResourceAsStream(commandSpec);
        if(cmdStream == null)
        {
            return null;
        }
        commandProperties.load(cmdStream);
        return  commandProperties;
    }



    public String processCommand(String command, Map<String, String> depsMap)
    {
        try {
            String cmd = command.replace("${JAVA}", javaBin).replace("${MVN}", mvnBin)
                    .replace("${CWD_URI}", new File(System.getProperty("user.dir")).toURI().toURL().toExternalForm());
            for (Map.Entry<String, String> entry : depsMap.entrySet()) {
                cmd = cmd.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            return cmd;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public int execute(Properties cmdProps, String[] args, Map<String, String> depsMap)
    {
        Map<String, String> replacements = new HashMap<String, String>(depsMap);
        for(int i = 0; i<args.length; i++)
        {
            replacements.put("arg"+i, args[i]);
        }
        boolean silent = Boolean.parseBoolean(cmdProps.getProperty("silent"));
        String cmd1 = cmdProps.getProperty("cmd");
        if(cmd1 != null)
        {
            cmd1 = cmd1.replace("${BUILDER_JAVA_OPTS}", (javaOpts==null || javaOpts.trim().length()==0)?"":(javaOpts+" "));
            List<String> cmdList = new ArrayList<String>();
            for(String part : cmd1.split(" "))
            {
                cmdList.add(processCommand(part, replacements));
            }
            return execute(cmdList, null, !silent);
        }
        return 0;
    }

    public int execute(List<String> commandLine, File workingDir, boolean printOutput)
    {
        try
        {
            ProcessBuilder appProcessBuilder = new ProcessBuilder(commandLine.toArray(new String[0]));
            appProcessBuilder.redirectErrorStream(true);
            appProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            if(printOutput)
            {
                appProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            }

            if(workingDir != null && workingDir.isDirectory())
            {
                appProcessBuilder.directory(workingDir);
            }

            final Process process = appProcessBuilder.start();

            Thread closeProcessThread = new Thread()
            {
                public void run()
                {
                    process.destroy();
                }
            };

            Runtime.getRuntime().addShutdownHook(closeProcessThread);

            InputStream inputStream = process.getInputStream();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder fullOutputBuilder = new StringBuilder("\n******************** " + new Date() +
                    " ********************\n");
            while ((line = streamReader.readLine()) != null)
            {
                if(printOutput)
                {
                    System.out.println(line);
                }
                fullOutputBuilder.append(line).append("\n");
            }
            process.waitFor();
            int exitValue = process.exitValue();
            if(exitValue != 0)
            {
                FileWriter logWriter = new FileWriter(errorLogName, appendToLog);
                appendToLog = true;
                logWriter.write(fullOutputBuilder.toString());
                logWriter.close();
            }
            return exitValue;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    protected void checkPrerequisites() throws Exception
    {
        javaBin = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";

        userHome = System.getProperty("user.home");

        javaOpts = System.getenv("BUILDER_JAVA_OPTS");

        InputStream defaultPropertiesAsStream = this.getClass().getResourceAsStream("/default.properties");
        if (defaultPropertiesAsStream == null)
        {
        	throw new IOException("Could not open default.properties!");
        }
		appProperties.load(defaultPropertiesAsStream);

        String configDirProp = appProperties.getProperty("configDir");
        if (configDirProp == null)
        	throw new Exception("Could not find property 'configDir' in default.properties!");
		configFilePath = configDirProp.replace("{{user.home}}", userHome);

        version = appProperties.getProperty("version");
        name = appProperties.getProperty("name");

        String cmdPart = appProperties.getProperty("cmdName");
        if(cmdPart.contains("."))
        {
            cmdPart = cmdPart.split(".")[0];
        }
        errorLogName =  cmdPart + "-error-log.txt";

        File configFileDir = new File(configFilePath);
        configFileDir.mkdirs();
        configFile = new File(configFileDir, appProperties.getProperty("configFile"));
        configFile.createNewFile();
        FileInputStream fileInputStream = new FileInputStream(configFile);
        configProperties.load(fileInputStream);
        fileInputStream.close();
        depVersionPropertiesFile = new File(configFilePath, "depVersions.txt");

        if(depVersionPropertiesFile.exists())
        {
            FileInputStream depVersionPropertiesFileIS = new FileInputStream(depVersionPropertiesFile);
            depVersionProperties.load(depVersionPropertiesFileIS);
            depVersionPropertiesFileIS.close();
        }
        lookupMavenBinary();
        String updateProp = configProperties.getProperty("updateInterval");
        if(updateProp != null)
        {
            try
            {
                updateInterval = Integer.valueOf(updateProp);
            }
            catch(NumberFormatException e)
            {
                System.out.println("Error parsing update interval");
            }
        }
    }

    protected void lookupMavenBinary() throws IOException
    {
        mvnBin = configProperties.getProperty("mvnBin");
        if(mvnBin == null || !(new File(mvnBin)).exists())
        {
            System.out.println("Maven binary not configured, scan in progress ...");
            String path = System.getenv("PATH");
            if(path != null)
            {
                String[] pathEntries = path.split(File.pathSeparator);
                for(String pathEntry : pathEntries)
                {
                    String mvn = executableExistsWithEnding(pathEntry + File.separatorChar + "mvn");
                    if(mvn != null)
                    {
                        System.out.println("Maven binary found: " + mvn);
                        mvnBin = mvn;
                        configProperties.setProperty("mvnBin", mvnBin);
                        FileOutputStream outputStream = new FileOutputStream(configFile);
                        configProperties.store(outputStream, "");
                        return;
                    }
                }

                System.out.println("ERROR: 'mvn' executable not found. Make sure you have maven 3 or higher installed and added to your PATH environment.");
            }
        }
    }

    private boolean isWindows()
    {
        return System.getProperty("os.name").contains("indows");
    }

    protected String executableExistsWithEnding(String filename)
    {
        String mvn = filename;
        File candidate = new File(filename);
        if(candidate.exists() && candidate.canExecute() && !isWindows())
        {
            return mvn;
        }

        mvn = filename + ".exe";
        candidate = new File(mvn);
        if(candidate.exists() && candidate.canExecute())
        {
            return mvn;
        }

        mvn = filename + ".bat";
        candidate = new File(mvn);
        if(candidate.exists() && candidate.canExecute())
        {
            return mvn;
        }

        mvn = filename + ".cmd";
        candidate = new File(mvn);
        if(candidate.exists() && candidate.canExecute())
        {
            return mvn;
        }

        return null;
    }

    /**
     * reading the generated version.txt from url
     * @return String version
     */
    private String readReleasedVersion(){
        String newestVersion="";
        networkConnection=true;
        try {
            URL url = new URL(versionUrl);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            String[] splitedLine;
            while ((line = bufferedReader.readLine()) != null) {
                splitedLine = line.split("=");
                if(splitedLine.length==2){
                    newestVersion = splitedLine[1];
                }
            }
            bufferedReader.close();
        }catch(UnknownHostException unknownHostException){
            System.out.println(ansi_blue + "Info: Cannot find host " + unknownHostException.getMessage() + " to check the " + name + " version."+ ansi_reset);
            networkConnection=false;
        }catch(Exception e){
            e.printStackTrace();
        }
        return newestVersion;
    }

}