/*******************************************************************************
 * Copyright 2017 M.S.Khan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package project.sudoku.exception;

/**
 * Exception thrown when some project resource fails to load.
 * 
 * @since
 * Dated - 13-Jul-2017
 * 
 * @author S.Khan
 *
 */
public class ResourceLoadingException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ResourceLoadingException()
    {
        super();
    }

    /**
     * 
     * @param message
     */
    public ResourceLoadingException(String message)
    {
        super(message);
    }

    /**
     * 
     * @param cause
     */
    public ResourceLoadingException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ResourceLoadingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
