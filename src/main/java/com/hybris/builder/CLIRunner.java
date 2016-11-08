package com.hybris.builder;

import sun.misc.IOUtils;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;



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
    private String ansi_yellow = "\u001B[33m";
    private String javaOpts;
    private String mvnOpts;
    private static final String VERSION_URL = "https://raw.githubusercontent.com/SAP/builder-cli/master/version.txt";
    private static final int TIMEOUT_VALUE = 5000;

    private boolean networkConnection = true;
    private boolean appendToLog = false;
    private Set<Closeable> streams = new HashSet();

    public static void main(String [ ] args)
    {
        new CLIRunner().initialize(args);
    }

    /**
     * The initialize method handle the command parameter and the optional parameters.
     * @param args Command and optional a parameter for the builder cli tool
     */
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
                ansi_yellow = "";
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
                else if("experimental".equals(args[0]))
                {
                    showUsage(null, false, null, true);
                    System.exit(0);
                }
                else
                {
                    String command = args[0];
                    Properties cmdProps = loadCommand(command);
                    if (cmdProps == null) {
                        showUsage(command, true);
                        System.exit(0);
                    }

                    if ("template".equals(cmdProps.getProperty("type"))) {
                        createTemplate(cmdProps, args);
                    } else {
                        String cmd = cmdProps.getProperty("cmd");
                        String deps = cmdProps.getProperty("dependencies");
                        if (((cmd != null && cmd.contains("${MVN}")) || (deps != null && deps.length() > 0)) && !lookupMavenBinary()) {
                            System.exit(1);
                        }

                        boolean depsFulfilled = true;

                        String extDepsProp = cmdProps.getProperty("external_dependencies");
                        if (extDepsProp != null && extDepsProp.length() > 0) {
                            List<String> extDeps = extDepsProp.contains(",") ? Arrays.asList(extDepsProp.split(",")) : Collections.singletonList(extDepsProp);
                            for (String dep : extDeps) {
                                if (lookupBinary(dep) == null) {
                                    System.out.println("Missing dependency: " + dep);
                                    if (cmdProps.containsKey("external_dependencies_res_" + dep)) {
                                        System.out.println("Trying to automatically resolve dependencies...");
                                        Properties depResCmd = new Properties();
                                        depResCmd.setProperty("cmd", cmdProps.getProperty("external_dependencies_res_" + dep));
                                        if (execute(depResCmd, null, Collections.<String, String>emptyMap()) > 0) {
                                            System.out.println("Failed. Please try to install dependencies manually.");
                                            depsFulfilled = false;
                                        }
                                    } else {
                                        depsFulfilled = false;
                                    }
                                }
                            }
                        }

                        if (!depsFulfilled) {
                            String extDepsHint = cmdProps.getProperty("external_dependencies_hint");
                            if (extDepsHint != null) {
                                System.out.println("\n" + extDepsHint);
                            }
                            System.exit(1);
                        }

                        int exitCode = execute(cmdProps, args, downloadDependencies(command, cmdProps));
                        if (appendToLog) {
                            System.out.println("\nERROR: Command failed. Check '" +
                                    errorLogName + "' for more info");
                        }
                        System.exit(exitCode);
                    }
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
        }finally {
            for(Closeable closeable : streams){
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     * Clear the cached libraries.
     */
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

    /**
     * Delete the directories of the cached libraries after clearing the cache
     * @param path Either this parameter is a file or a directory. It will delete all.
     */
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

    /**
     * Creates a directory named of the argument and generate files in the created directory based on the files in the template folder.
     * @param cmdProps Properties of the cmd in the corresponding property file.
     * @param args Argument for the createModule command.
     * @throws IOException
     */
    private void createTemplate(Properties cmdProps, String[] args) throws IOException
    {
        boolean wrongNumberOfArguments = false;
        Map<String, String> argsMap = new HashMap<String, String>();

        if(args.length < 2) {
            wrongNumberOfArguments = true;
        } else {
            for (int k = 1; cmdProps.containsKey("arg" + k); k++) {
                if (k >= args.length) {
                    wrongNumberOfArguments = true;
                    break;
                }
                argsMap.put("ARG" + k, args[k]);
            }
        }

        if(wrongNumberOfArguments) {
            showUsage(args[0], true, "Error: Wrong number of arguments.", false);
            System.exit(0);
        }

        System.out.println("\nAvailable templates: \n");
        String templateName = null;
        Properties defaultProperties = new Properties();
        defaultProperties.load(addToStreamStack(getClass().getResourceAsStream("/default.properties")));
        String templateList = defaultProperties.getProperty("templates");
        String[] templates = templateList.split(",");


        for(int i = 0; i<templates.length;i++){
            Properties props = new Properties();
            props.load(addToStreamStack(getClass().getClassLoader().getResourceAsStream("templates/" + templates[i] + "/template.properties")));
            String description = props.getProperty("description");
            System.out.println(ansi_white + i + ". " + templates[i] + ansi_reset + " - " + description);
        }

        int templNr = -1;

        do {
            System.out.print("\nEnter a number" + ansi_white + "\n> " + ansi_reset);
            String input = System.console().readLine();
            try {
                templNr = Integer.parseInt(input);
                if(templNr < 0 || templNr >= templates.length) {
                    templNr = -1;
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number between 0 and " + (templates.length - 1));
            }
        } while (templNr < 0);
        templateName = templates[templNr];

        Properties templateProps = new Properties();
        templateProps.load(addToStreamStack(getClass().getClassLoader().getResourceAsStream("templates/" + templateName + "/template.properties")));

        String rawdirs = templateProps.getProperty("dirs");
        String rawfiles = templateProps.getProperty("files");

        if(templateProps.containsKey("replacements")) {
            System.out.println("This template allows you to specify replacement values for variables within the resource files. If you don't want to set them now, just leave them empty.\n");
            String replacements = templateProps.getProperty("replacements");
            List<String> replList = new ArrayList<String>();
            if(replacements.contains(",")) {
                replList.addAll(Arrays.asList(replacements.split(",")));

            } else {
                replList.add(replacements);
            }

            for (String repl : replList) {
                String description = templateProps.getProperty(repl + "_description");
                if(description == null) {
                    description = repl;
                }
                System.out.print(description + ansi_white + "\n> " + ansi_reset);

                argsMap.put(repl, System.console().readLine());
            }

        }

        //create target directory
        File targetDirectory = new File(args[1]);

        if(targetDirectory.exists())
        {
            System.out.println("Error: Directory already exists: " + targetDirectory.getAbsolutePath());
            System.exit(1);
        }
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

                String template = getString(addToStreamStack(getClass().getClassLoader()
                        .getResourceAsStream("templates/" + templateName + "/" + filename)));

                if(template != null)
                {
                    writeStringToFile(processTemplate(template, argsMap), file);
                }
            }
        }
    }

    /**
     * Convert the inputStream parameter to a string.
     * @param inputStream Template files as stream
     * @return InputStream as string.
     * @throws IOException
     */
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

    /**
     * Show usage of the command line tool without an error Message.
     * @param command The command, which the command line tool should perform.
     * @param error true, if an error notification should be printed out
     * @throws IOException
     */
    protected void showUsage(String command, boolean error) throws IOException
    {
        showUsage(command, error, null, false);
    }

    /**
     * Shows the usage of the builder cli tool, either only "builder" was typed in the console or wrong parameter was passed.
     * @param command The command, which the builder cli should perform.
     * @param error Boolean value, if an error occurred.
     * @param errorMsg Print a error message, if something went wrong.
     * @param experimental Show experimental commands only.
     * @throws IOException
     */
    protected void showUsage(String command, boolean error, String errorMsg, boolean experimental) throws IOException
    {
        System.out.println(ansi_white + name + ansi_reset + ", version " + version +"\n"+"Copyright (c) 2009-2016 SAP SE or an SAP affiliate company\n");
        String readReleasedVersion = readReleasedVersion();
        if(!readReleasedVersion.equals(version) && networkConnection==true){
            System.out.println(ansi_blue + name + " is outdated. Please download the newest "+ readReleasedVersion +" version!!!" + ansi_reset);
        }

        if(error)
        {
            String msg = errorMsg == null ? "ERROR: Unknown command '" + command + "'" : errorMsg;
            System.out.println(ansi_red + msg + ansi_reset);
        }

        if(experimental)
        {
            System.out.println("\n" + ansi_yellow + "Experimental commands (no support, use at your own risk)" + ansi_reset);
        }

        System.out.println("\nUsage: " + appProperties.getProperty("cmdName") + " command [arg ...]");
        String commands = appProperties.getProperty(experimental ? "experimentalCommands" : "commands");
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
        if(!experimental)
        {
            System.out.println("\t" + ansi_white + "displayCachedVersions" + ansi_reset + "\t\t" + "Print a list with versions of cached dependencies" + "\n");
            System.out.println("\t" + ansi_white + "clearCache" + ansi_reset + "\t\t\t" + "Delete cached dependencies" + "\n");
            System.out.println("\t" + ansi_white + "experimental" + ansi_reset + "\t\t\t" + "Show experimental commands (no support, use at your own risk)" + "\n");
        }
        System.out.flush();
    }

    /**
     * Replace the placeholders from the pom_template file with parameters.
     * @param template Template of the pom file.
     * @param params Parameters for the pom file.
     * @return a pom file as string with all replaced parameters.
     */
    protected String processTemplate(String template, Map<String, String> params)
    {
        String processed = template;
        for(Map.Entry<String, String> param : params.entrySet())
        {
            String value = param.getValue();
            if(value == null || value.trim().length() == 0) {
                value = param.getKey();
            }
            processed = processed.replaceAll("\\{\\{" + param.getKey() + "}}", value);
        }
        return processed;
    }

    /**
     * Download the corresponding dependencies for the command and save the alias and absolute path of the dependency in a map.
     * @param command The command, which the builder cli should perform.
     * @param cmdProps The dependencies which are defined in the corresponding command property file.
     * @return A map with all maven dependencies which are necessary for the command.
     * @throws IOException
     */
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

            InputStream pomStream = addToStreamStack(getClass().getResourceAsStream("/pom_template.xml"));

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
                    commonDepProperties.load(addToStreamStack(getClass().getResourceAsStream("/commonDependencies.properties")));
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

                    List<String> cmdList = new ArrayList<String>();

		    if(isWindows()) {
		        cmdList.add("cmd");
			cmdList.add("/c");
		    }

                    cmdList.add(mvnBin);

                    if(mvnOpts != null && mvnOpts.trim().length() > 0) {
                        if(mvnOpts.contains(" ")) {
                            String[] opts = mvnOpts.split(" ");
                            for(String opt : opts) {
                                if(opt != null && opt.trim().length() > 0) {
                                    cmdList.add(opt);
                                }
                            }
                        } else {
                            cmdList.add(mvnOpts);
                        }
                    }

                    cmdList.addAll(Arrays.asList("-U", "-DoutputDirectory=" + tmpDir.getAbsolutePath(),
                            "-DincludeArtifactIds=" + artifactId,
                            "dependency:copy-dependencies"));

                    int exitValue = execute(cmdList, tmpDir, printOutput);

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
        depVersionProperties.store(addToStreamStack(new FileOutputStream(depVersionPropertiesFile)), "");
        return depsMap;
    }

    /**
     * Save the config file.
     */
    private void saveConfigFile() {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            addToStreamStack(fos);
            configProperties.store(fos, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the content to a file.
     * @param content The Content, which should be written to the file.
     * @param file The file, where the content should be written.
     * @throws IOException
     */
    protected void writeStringToFile(String content, File file) throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        addToStreamStack(writer);
        writer.write(content);
        writer.flush();
    }

    /**
     * Load a command and read all properties of the corresponding command.properties file.
     * @param command The command, where the properties of the corresponding command should be loaded.
     * @return Return all the properties of the command.properties file.
     * @throws IOException
     */
    protected Properties loadCommand(String command) throws IOException {
        String commandSpec = "/commands/" + command + ".properties";
        Properties commandProperties = new Properties();
        InputStream cmdStream = addToStreamStack(this.getClass().getResourceAsStream(commandSpec));
        if(cmdStream == null)
        {
            return null;
        }
        commandProperties.load(cmdStream);
        addToStreamStack(cmdStream);
        return  commandProperties;
    }

    /**
     * Read the cmd property of the command properties file. If there are any dependencies like java or maven they will be resolved.
     * @param command The command, which the builder cli should perform.
     * @param depsMap A map of all dependendies of the current command.
     * @return The command, that should be proccessed.
     */
    public String processCommand(String command, Map<String, String> depsMap)
    {
        try {
            String cmd = command.replace("${JAVA}", javaBin).replace("${MVN}", mvnBin == null ? "" : mvnBin)
                    .replace("${CWD_URI}", new File(System.getProperty("user.dir")).toURI().toURL().toExternalForm())
                    .replace("${CFGDIR_SEP}", configFilePath + File.separator).replace("${SEP}", File.separator);
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

    /**
     * Read the properties {cmd} and {arg} of the command.properties which will be executed. After all dependencies are resolved all single commands saved in a List and return the exit value for the subprocess.
     * @param cmdProps Properties of the cmd in the corresponding property file.
     * @param args Additional arguments for the command.
     * @param depsMap  A map of all dependendies of the current command.
     * @return Returns the exit value for the subprocess. By convention, the value indicates normal termination.
     */
    public int execute(Properties cmdProps, String[] args, Map<String, String> depsMap)
    {
        Map<String, String> replacements = new HashMap<String, String>(depsMap);
        if(args != null) {
            for (int i = 0; i < args.length; i++) {
                replacements.put("arg" + i, args[i]);
            }
        }
        boolean silent = Boolean.parseBoolean(cmdProps.getProperty("silent"));
        String cmd1 = cmdProps.getProperty("cmd");
        if(cmd1 != null)
        {
            cmd1 = cmd1.replace("${BUILDER_JAVA_OPTS}", (javaOpts==null || javaOpts.trim().length()==0)?"":(javaOpts+" "));
            cmd1 = cmd1.replace("${BUILDER_MVN_OPTS}", (mvnOpts==null || mvnOpts.trim().length()==0)?"":(mvnOpts+" "));
            List<String> cmdList = new ArrayList<String>();
            for(String part : cmd1.split(" "))
            {
                cmdList.add(processCommand(part, replacements));
            }

            if(cmdList.size() > 0 )
            {
                if("node".equals(cmdList.get(0)))
                {
                    copyNodeScripts();
                }
                else if("http-server".equals(cmdList.get(0)))
                {
                    copyCommonResources();
                }
                if("skeletor".equals(cmdList.get(2))){
                    copySkeletor();
                }
            }

            if(isWindows())
            {
                cmdList.addAll(0, Arrays.asList("cmd.exe", "/C"));
            }

            String preCmdEcho = cmdProps.getProperty("pre-cmd-echo");
            if(preCmdEcho != null && preCmdEcho.trim().length() > 0) {
                System.out.println(preCmdEcho.replace("${selfSignedCertFingerprint}", appProperties.getProperty("selfSignedCertFingerprint")));
            }

            return execute(cmdList, null, !silent);
        }
        return 0;
    }

    /**
     * Execute the commandLine. See the constructor of {@ProcessBuilder}.
     * @param commandLine The command, that should be executed.
     * @param workingDir the new working directory.
     * @param printOutput if output should be printed.
     * @return Returns the exit value for the subprocess. By convention, the value indicates normal termination.
     */
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
            addToStreamStack(inputStream);
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
                addToStreamStack(logWriter);
                logWriter.write(fullOutputBuilder.toString());
                logWriter.flush();
            }
            return exitValue;

        }
        catch (Exception e)
        {
            if(printOutput) {
                e.printStackTrace();
            }
            return 1;
        }
    }

    /**
     * Check if all prerequisites like java and maven are installed.
     * @throws Exception, if something went wrong.
     */
    protected void checkPrerequisites() throws Exception
    {
        javaBin = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";

        userHome = System.getProperty("user.home");

        javaOpts = System.getenv("BUILDER_JAVA_OPTS");
        mvnOpts = System.getenv("BUILDER_MVN_OPTS");

        InputStream defaultPropertiesAsStream = null;
        defaultPropertiesAsStream = this.getClass().getResourceAsStream("/default.properties");
        addToStreamStack(defaultPropertiesAsStream);
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
        addToStreamStack(fileInputStream);
        configProperties.load(fileInputStream);
        fileInputStream.close();
        depVersionPropertiesFile = new File(configFilePath, "depVersions.txt");

        if(depVersionPropertiesFile.exists())
        {
            FileInputStream depVersionPropertiesFileIS = new FileInputStream(depVersionPropertiesFile);
            addToStreamStack(depVersionPropertiesFileIS);
            depVersionProperties.load(depVersionPropertiesFileIS);
        }
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


    /**
     * Copies all common resources to USERHOME/.builder/res
     */
    protected void copyCommonResources()
    {
        copyFiles("commonResources", "common/", new File(configFilePath, "res"));
    }

    /**
     * Copies all node scripts to USERHOME/.builder/scripts/node
     */
    protected void copyNodeScripts()
    {
        copyFiles("nodescripts", "scripts/node/", new File(configFilePath, "scripts" + File.separator + "node"));
    }

    protected void copySkeletor(){
        copyFiles("skeletorfiles", "skeletorResources/", new File(configFilePath, "skeletor"));
        copyFiles("skeletorcmpfiles", "skeletorResources/snippets/", new File(configFilePath, "skeletor"+ File.separator + "snippets"));
    }

    /**
     * Copies resources files to the specified output directory.
     * @param propertyKey the key defining the resources value in default.properties.
     * @param resourcePath the path where the resources are located.
     * @param outputDir the directory where the resources should be copied to (will be created if it doesn't exist).
     */
    protected void copyFiles(final String propertyKey, final String resourcePath, final File outputDir)
    {
        String fileListString = appProperties.getProperty(propertyKey);
        if(fileListString != null && !fileListString.isEmpty())
        {
            List<String> files = new ArrayList<String>();
            if(fileListString.contains(","))
            {
                files.addAll(Arrays.asList(fileListString.split(",")));
            }
            else
            {
                files.add(fileListString);
            }

            outputDir.mkdirs();

            for (String fileName : files)
            {
                try {
                    File file = new File(outputDir, fileName);

                    file.createNewFile();

                    String fileContent = getString(addToStreamStack(getClass().getClassLoader()
                            .getResourceAsStream(resourcePath + fileName)));

                    if (fileContent != null) {
                        writeStringToFile(fileContent, file);
                    }
                }
                catch (IOException e)
                {
                    System.out.println("Error: Could not create file: " + fileName);
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Save the absolute path of the maven binary to config.properties.
     * @throws IOException
     */
    protected boolean lookupMavenBinary() throws IOException
    {
        mvnBin = configProperties.getProperty("mvnBin");
        if(mvnBin == null || !(new File(mvnBin)).exists())
        {
            System.out.println("Maven binary not configured, scan in progress ...");

            String mvn = lookupBinary("mvn");
            if(mvn != null)
            {
                System.out.println("Maven binary found: " + mvn);
                mvnBin = mvn;
                configProperties.setProperty("mvnBin", mvnBin);
                FileOutputStream outputStream = addToStreamStack(new FileOutputStream(configFile));
                configProperties.store(outputStream, "");
                outputStream.flush();
                return true;
            }


            System.out.println("ERROR: 'mvn' executable not found. Make sure you have maven 3 or higher installed and added to your PATH environment.");
            return false;
        }
        return true;
    }

    protected String lookupBinary(final String bin) throws IOException
    {
        String path = System.getenv("PATH");
        if(path != null)
        {
            String[] pathEntries = path.split(File.pathSeparator);
            for(String pathEntry : pathEntries)
            {
                String binPath = executableExistsWithEnding(pathEntry + File.separatorChar + bin);
                if(binPath != null)
                {
                    return binPath;
                }
            }

        }
        return null;
    }

    /**
     * Check if the operating system is a windows machine.
     * @return true if the operating system is a windows machine.
     */
    private boolean isWindows()
    {
        return System.getProperty("os.name").contains("indows");
    }

    /**
     * Returns the absolute path of a binary file independent of the operating system.
     * @param filename
     * @return the absolute path to the binary file as a string.
     */
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
     * Reading the generated version.txt from url.
     * @return String version
     */
    private String readReleasedVersion(){
        String newestVersion="";
        networkConnection=true;
        try {
            URLConnection connection = new URL(VERSION_URL).openConnection();
            connection.setConnectTimeout(TIMEOUT_VALUE);
            connection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(addToStreamStack(connection.getInputStream())));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitLine = line.split("=");
                if(splitLine.length==2){
                    newestVersion = splitLine[1];
                }
            }
            bufferedReader.close();
        }catch(UnknownHostException unknownHostException){
            System.out.println(ansi_blue + "Info: Cannot find host " + unknownHostException.getMessage() + " to check the " + name + " version."+ ansi_reset);
            networkConnection=false;
        }catch(SocketTimeoutException ste){
            System.out.println(ansi_blue + "Info: Connection timed out after " + (TIMEOUT_VALUE / 1000) + " seconds. Please check your network settings."+ ansi_reset);
            networkConnection=false;
        }catch(Exception e){
            e.printStackTrace();
        }
        return newestVersion;
    }

    private <T extends Closeable> T addToStreamStack(T closeable){
        streams.add(closeable);
        return closeable;
    }
}