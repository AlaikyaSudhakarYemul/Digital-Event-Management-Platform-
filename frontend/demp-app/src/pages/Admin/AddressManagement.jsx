import React from 'react';
 
const AddressManagement = ({
  addressForm,
  handleAddressChange,
  handleAddressSubmit,
  addresses,
  handleAddressEdit,
  handleAddressDelete,
  editingAddressId,
  addressMessage
}) => {
  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">
        {editingAddressId ? 'Edit Address' : 'Add Address'}
      </h2>
      <form onSubmit={handleAddressSubmit} className="space-y-4">
        <input
          type="text"
          name="address"
          value={addressForm.address}
          onChange={handleAddressChange}
          placeholder="Enter address"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <input
          type="text"
          name="state"
          value={addressForm.state}
          onChange={handleAddressChange}
          placeholder="Enter state"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <input
          type="text"
          name="country"
          value={addressForm.country}
          onChange={handleAddressChange}
          placeholder="Enter country"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <input
          type="text"
          name="pincode"
          value={addressForm.pincode}
          onChange={handleAddressChange}
          placeholder="Enter pincode"
          className="w-full px-4 py-2 rounded bg-gray-700 border border-gray-600"
        />
        <button
          type="submit"
          className="w-full bg-cyan-500 hover:bg-cyan-600 text-white font-semibold py-2 px-4 rounded-lg"
        >
          {editingAddressId ? 'Update Address' : 'Add Address'}
        </button>
        {addressMessage && (
          <p className="text-center text-sm mt-2 text-yellow-400">{addressMessage}</p>
        )}
      </form>
 
      <hr className="my-6 border-gray-600" />
 
      <h2 className="text-xl font-semibold mb-4">Existing Addresses</h2>
      <ul className="space-y-2">
        {addresses.map((addr) => (
          <li key={addr.addressId} className="bg-gray-700 p-3 rounded flex justify-between items-start">
            <div>
              <p className="font-bold">{addr.address}</p>
              <p className="text-sm text-gray-300">
                {addr.state}, {addr.country} - {addr.pincode}
              </p>
            </div>
            <div className="space-x-2">
              <button
                onClick={() => handleAddressEdit(addr)}
                className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded"
              >
                Edit
              </button>
              <button
                onClick={() => handleAddressDelete(addr.addressId)}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};
 
export default AddressManagement;
 