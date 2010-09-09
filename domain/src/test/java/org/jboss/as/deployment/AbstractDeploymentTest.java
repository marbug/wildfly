/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.deployment;

import org.jboss.as.deployment.module.DeploymentModuleLoaderSelector;
import org.jboss.as.model.ServerGroupDeploymentElement;
import org.jboss.modules.Module;
import org.jboss.msc.service.BatchBuilder;
import org.jboss.msc.service.ServiceActivatorContextImpl;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jboss.as.deployment.TestUtils.getResource;
import static org.junit.Assert.fail;

/**
 * Abstract test base for deployment tests.
 *
 * @author John E. Bailey
 */
public abstract class AbstractDeploymentTest {
    protected static final byte[] BLANK_SHA1 = new byte[20];

    protected ServiceContainer serviceContainer;

    @Before
    public void setup() throws Exception {
        System.setProperty("jboss.server.deploy.dir", VFS.getChild(getResource(AbstractDeploymentTest.class, "/test")).getPathName());
        Module.setModuleLoaderSelector(new DeploymentModuleLoaderSelector());

        serviceContainer = ServiceContainer.Factory.create();

        runWithLatchedBatch(new BatchedWork() {
            @Override
            public void execute(BatchBuilder batchBuilder) throws Exception {
                setupServices(batchBuilder);
            }
        });
    }

    protected void setupServices(final BatchBuilder batchBuilder) throws Exception {
    }

    @After
    public void shutdown() {
        serviceContainer.shutdown();
    }

    protected void executeDeployment(final VirtualFile deploymentRoot) throws Exception {
        runWithLatchedBatch(new BatchedWork() {
            public void execute(BatchBuilder batchBuilder) throws Exception {
                new ServerGroupDeploymentElement(null, deploymentRoot.getName(), BLANK_SHA1, true).activate(new ServiceActivatorContextImpl(batchBuilder));
            }
        });
    }

    protected String getDeploymentName(VirtualFile deploymentRoot) {
        return deploymentRoot.getName().replace('.', '_');
    }

    protected void runWithLatchedBatch(final BatchedWork work) throws Exception {
        final BatchBuilder batchBuilder = serviceContainer.batchBuilder();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        final TestServiceListener listener = new TestServiceListener(new Runnable() {
            public void run() {
                completed.set(true);
                latch.countDown();
            }
        });
        batchBuilder.addListener(listener);
        // Run the work
        work.execute(batchBuilder);

        batchBuilder.install();
        listener.finishBatch();
        latch.await(5L, TimeUnit.SECONDS);
        if (!completed.get())
            fail("Did not install deployment within 5 seconds.");
    }

    protected static interface BatchedWork {
        void execute(final BatchBuilder batchBuilder) throws Exception;
    }
}
