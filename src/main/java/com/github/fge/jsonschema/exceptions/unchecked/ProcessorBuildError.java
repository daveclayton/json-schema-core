/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.exceptions.unchecked;

import com.github.fge.jsonschema.processing.ProcessorChain;
import com.github.fge.jsonschema.processing.ProcessorMap;
import com.github.fge.jsonschema.processing.ProcessorSelector;
import com.github.fge.jsonschema.report.ProcessingMessage;

/**
 * Exception thrown by processor builders on anomalous inputs
 *
 * @see ProcessorChain
 * @see ProcessorSelector
 * @see ProcessorMap
 */
public final class ProcessorBuildError
    extends ProcessingConfigurationError
{
    public ProcessorBuildError(final ProcessingMessage message)
    {
        super(message);
    }
}