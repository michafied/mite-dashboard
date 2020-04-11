package biz.schroeders.mite;

/*
    This file is part of mite-dashboard.

    mite-dashboard is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mite-dashboard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with mite-dashboard.  If not, see <http://www.gnu.org/licenses/>.
*/

public class ApiError extends RuntimeException {
    private final int httpCode;

    public ApiError(final String error, final int httpCode) {
        super("{\"error\":\"" + error + "\"}");
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
