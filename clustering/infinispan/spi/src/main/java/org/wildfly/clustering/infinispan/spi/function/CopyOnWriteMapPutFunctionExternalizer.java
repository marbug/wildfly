/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.infinispan.spi.function;

import java.util.Map;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;

/**
 * Externalizer for instances of {@link CopyOnWriteMapPutFunction}.
 * @author Paul Ferraro
 * @param <K> the map key type
 * @param <V> the map value type
 */
@MetaInfServices(Externalizer.class)
public class CopyOnWriteMapPutFunctionExternalizer<K, V> extends AbstractFunctionExternalizer<Map.Entry<K, V>, Map<K, V>, CopyOnWriteMapPutFunction<K, V>> {

    @SuppressWarnings("unchecked")
    @Override
    public Class<CopyOnWriteMapPutFunction<K, V>> getTargetClass() {
        return (Class<CopyOnWriteMapPutFunction<K, V>>) (Class<?>) CopyOnWriteMapPutFunction.class;
    }

    @Override
    public CopyOnWriteMapPutFunction<K, V> apply(Map.Entry<K, V> entry) {
        return new CopyOnWriteMapPutFunction<>(entry);
    }
}
