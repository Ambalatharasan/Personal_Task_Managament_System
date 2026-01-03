package Project.TaskAutomation;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import Project.config.AppConfig;
import Project.config.QuartzConfig;
import Project.config.SecurityConfig;
import Project.config.WebConfig;
import jakarta.servlet.ServletContext;

public class MainApplication {

	public static void main(String[] args) {
		try {
			System.out.println("--- Starting Task Automation System... ---");
			int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
			System.out.println("Configured port: " + port);

			Tomcat tomcat = new Tomcat();
			tomcat.setPort(port);
			tomcat.getConnector();
			String baseDir = new File("target/tomcat").getAbsolutePath();
			tomcat.setBaseDir(baseDir);

			File webappDir = new File("src/main/webapp");
			if (!webappDir.exists()) {
				boolean created = webappDir.mkdirs();
				if (created) {
					System.out.println("-> Created webapp directory: " + webappDir.getAbsolutePath());
				}
			}

			String contextPath = "";
			String docBase = webappDir.getAbsolutePath();
			Context tomcatContext = tomcat.addContext(contextPath, docBase);
			ServletContext servletContext = tomcatContext.getServletContext();

			AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
			rootContext.register(AppConfig.class, SecurityConfig.class, QuartzConfig.class);
			rootContext.setServletContext(servletContext);
			rootContext.refresh();
			servletContext.setAttribute("org.springframework.web.context.WebApplicationContext.ROOT", rootContext);

			AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
			webContext.register(WebConfig.class);
			webContext.setParent(rootContext);
			webContext.setServletContext(servletContext);

			DelegatingFilterProxy securityFilter = new DelegatingFilterProxy("springSecurityFilterChain", rootContext);
			FilterDef filterDef = new FilterDef();
			filterDef.setFilterName("springSecurityFilterChain");
			filterDef.setFilter(securityFilter);
			tomcatContext.addFilterDef(filterDef);

			FilterMap filterMap = new FilterMap();
			filterMap.setFilterName("springSecurityFilterChain");
			filterMap.addURLPattern("/*");
			tomcatContext.addFilterMap(filterMap);

			DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);
			Tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet);
			tomcatContext.addServletMappingDecoded("/", "dispatcher");

			try {
				org.quartz.Scheduler scheduler = rootContext.getBean(org.quartz.Scheduler.class);
				scheduler.start();
				System.out.println("-> Quartz Scheduler started successfully");
			} catch (Exception e) {
				System.err.println("!! Failed to start Quartz Scheduler: " + e.getMessage());
			}

			tomcat.start();

			System.out.println("");
			System.out.println("===============================================");
			System.out.println(" System Started Successfully!");
			System.out.println("===============================================");
			System.out.println("");

			tomcat.getServer().await();

		} catch (LifecycleException e) {
			System.err.println("");
			System.err.println("!! Failed to start Tomcat server:");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("");
			System.err.println("!! Failed to initialize application:");
			e.printStackTrace();
			System.exit(1);
		}
	}
}