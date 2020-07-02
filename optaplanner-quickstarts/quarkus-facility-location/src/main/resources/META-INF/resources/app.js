const colors = [
  'aquamarine',
  'blueviolet',
  'chocolate',
  'cornflowerblue',
  'crimson',
  'forestgreen',
  'gold',
  'lawngreen',
  'orange',
  'tomato',
];
let autoRefreshCount = 0;
let autoRefreshIntervalId = null;

const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const facilitiesTable = $('#facilities');

const colorById = (i) => colors[i % colors.length];
const colorByConsumer = (consumer) => consumer.facility === null ? {} : { color: colorById(consumer.facility.id) };

const fetchHeaders = {
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
};

const getStatus = () => {
  fetch('/flp/status', fetchHeaders)
    .then((response) => {
      if (!response.ok) {
        handleErrorResponse('Get status failed', response);
      } else {
        // TODO avoid nesting
        response.json().then((data) => showProblem(data));
      }
    })
    .catch((error) => console.error('Failed to process response', error));
};

const solve = () => {
  fetch('/flp/solve', { ...fetchHeaders, method: 'POST' })
    .then((response) => {
      if (!response.ok) {
        handleErrorResponse('Start solving failed', response);
      } else {
        updateSolvingStatus(true);
        autoRefreshCount = 300;
        if (autoRefreshIntervalId == null) {
          autoRefreshIntervalId = setInterval(autoRefresh, 500);
        }
      }
    })
    .catch((error) => console.error('Failed to process response', error));
};

const stopSolving = () => {
  fetch('/flp/stopSolving', { ...fetchHeaders, method: 'POST' })
    .then((response) => {
      if (!response.ok) {
        handleErrorResponse('Stop solving failed', response);
      } else {
        updateSolvingStatus(false);
        getStatus();
      }
    })
    .catch((error) => console.error('Failed to process response', error));
};

const formatErrorResponseBody = (body) => {
  // JSON must not contain \t (Quarkus bug)
  const json = JSON.parse(body.replace(/\t/g, '  '));
  return `${json.details}\n${json.stack}`;
};

const handleErrorResponse = (title, response) => {
  response.text()
    .then((body) => {
      const message = `${title} (${response.status}: ${response.statusText}).`;
      const stackTrace = body ? formatErrorResponseBody(body) : '';
      showError(message, stackTrace);
    })
    .catch((error) => console.error('Failed to process response body', error));
};

const showError = (message, stackTrace) => {
  const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
    .append($(`<div class="toast-header bg-danger">
<strong class="mr-auto text-dark">Error</strong>
<button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
<span aria-hidden="true">&times;</span>
</button>
</div>`))
    .append($(`<div class="toast-body"/>`)
      .append($(`<p/>`).text(message))
      .append($(`<pre/>`)
        .append($(`<code/>`).text(stackTrace)),
      ),
    );
  $('#notificationPanel').append(notification);
  notification.toast({ autohide: false });
  notification.toast('show');
};

const updateSolvingStatus = (solving) => {
  if (solving) {
    solveButton.hide();
    stopSolvingButton.show();
  } else {
    autoRefreshCount = 0;
    solveButton.show();
    stopSolvingButton.hide();
  }
};

const autoRefresh = () => {
  getStatus();
  autoRefreshCount--;
  if (autoRefreshCount <= 0) {
    clearInterval(autoRefreshIntervalId);
    autoRefreshIntervalId = null;
  }
};

const facilityPopupContent = (facility) => `<h5>Facility ${facility.id}</h5>
<ul class="list-unstyled">
<li>Usage: ${facility.usedCapacity}/${facility.capacity}</li>
<li>Setup cost: ${facility.setupCost}</li>
</ul>`;

const showProblem = ({ solution, scoreExplanation, isSolving }) => {
  markerGroup.clearLayers();
  map.fitBounds(solution.bounds);
  facilitiesTable.children().remove();
  solution.facilities.forEach((facility) => {
    const { id, location, setupCost, capacity, usedCapacity, used } = facility;
    const percentage = usedCapacity / capacity * 100;
    L.marker(location)
      .addTo(markerGroup)
      .bindPopup(facilityPopupContent(facility));
    facilitiesTable.append(`<tr class="${used ? 'table-active' : 'text-muted'}">
<td>Facility ${id}</td>
<td><div class="progress">
<div class="progress-bar" role="progressbar" style="width: ${percentage}%" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">${usedCapacity}/${capacity}</div>
</div></td>
<td>$${setupCost}</td>
</tr>`);
  });
  solution.consumers.forEach((consumer) => {
    const color = colorByConsumer(consumer);
    L.circleMarker(consumer.location, color).addTo(markerGroup);
    if (consumer.facility !== null) {
      L.polyline([consumer.location, consumer.facility.location], color).addTo(markerGroup);
    }
  });
  $('#score').text(solution.score);
  $('#cost').text(solution.totalCost);
  $('#cost-percentage').text(Math.round(solution.totalCost * 1000 / solution.potentialCost) / 10);
  $('#distance').text(solution.totalDistance);
  $('#scoreInfo').text(scoreExplanation);
  updateSolvingStatus(isSolving);
};

const map = L.map('map', { doubleClickZoom: false }).setView([51.505, -0.09], 13);
map.whenReady(getStatus);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

const markerGroup = L.layerGroup();
markerGroup.addTo(map);

solveButton.click(solve);
stopSolvingButton.click(stopSolving);

updateSolvingStatus();
